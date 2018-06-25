package simulator.bitcoin.core;

import peersim.config.FastConfig;
import peersim.core.CommonState;
import peersim.core.Linkable;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.edsim.EDSimulator;
import simulator.bitcoin.core.events.BlockFoundEvent;
import simulator.bitcoin.core.messages.*;
import simulator.bitcoin.core.model.*;
import simulator.initializers.StartEvent;
import simulator.utilities.peersim.Distributions;
import simulator.utilities.structures.CircularQueue;
import simulator.utilities.structures.IntQueue;

import java.util.BitSet;
import java.util.LinkedList;
import java.util.List;

import static simulator.utilities.peersim.ConfigurationHelper.readInt;
import static simulator.utilities.peersim.NetworkUtilities.broadcast;
import static simulator.utilities.peersim.NetworkUtilities.send;

/**
 * Bitcoin core protocol to handle transactions, blocks and mining.
 */
public final class BitcoinCore implements EDProtocol, Cloneable {

	// parameters
	private static final String PARAMETER_MEAN = "mean";
	private static final String PARAMETER_BLOCK_SIZE = "block_size";

	// default configuration
	private static final int DEFAULT_MEAN = 20 * 60 * 1000;        // 20 minutes
	private static final int DEFAULT_BLOCK_SIZE = 20;

	// configuration actual values, extracted from configuration and defaults
	private final int mean;
	private final int maxBlockSize;

	// current status of the protocol
	public Blockchain blockchain;
	private BitSet knowTransactions;
	private Block miningFromBlock;
	private BlockFoundEvent lastBlockFoundEvent;
	private CircularQueue<Block> blocksToProcess;
	private BitSet gossipedBlocks;
	private BitSet gossipedTransactions;

	public BitcoinCore(String prefix) {

		// read configuration
		this.mean = readInt(prefix, PARAMETER_MEAN, DEFAULT_MEAN);
		this.maxBlockSize = readInt(prefix, PARAMETER_BLOCK_SIZE, DEFAULT_BLOCK_SIZE);

		// status
		this.blockchain = new Blockchain(Blocks.GENESIS);
		this.knowTransactions = new BitSet();
		this.miningFromBlock = Blocks.GENESIS;
		this.blocksToProcess = new CircularQueue<>();
		this.lastBlockFoundEvent = null;
		this.gossipedBlocks = new BitSet();
		this.gossipedTransactions = new BitSet();
	}

	private static void sendCheckLinkable(Node from, Node to, int pid, Object message) {
		final Linkable linkable = (Linkable) from.getProtocol(FastConfig.getLinkable(pid));
		assert linkable.contains(to);
		send(from, to, pid, message);
	}

	@Override
	public Object clone() {
		try {
			// NB: Block and BlockFoundEvent are immutable, so we do NOT need to clone them!
			// NB: lastProcessedTransactionIndex is a primitive type, Object.clone() will take care of it!
			final BitcoinCore clone = (BitcoinCore) super.clone();
			clone.blockchain = new Blockchain(this.blockchain);
			clone.knowTransactions = (BitSet) this.knowTransactions.clone();
			clone.blocksToProcess = new CircularQueue<>(this.blocksToProcess);
			clone.gossipedBlocks = (BitSet) this.gossipedBlocks.clone();
			clone.gossipedTransactions = (BitSet) this.gossipedTransactions.clone();
			return clone;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException("Clone not supported", e);
		}
	}


	// ------------------------------------------------------------------------------
	//  Events
	// ------------------------------------------------------------------------------

	@Override
	public void processEvent(Node me, int pid, Object event) {

		// messages
		if (event instanceof GetBlocksMessage) {
			onGetBlocksMessage(me, pid, (GetBlocksMessage) event);
		} else if (event instanceof InvMessage) {
			onInvMessage(me, pid, (InvMessage) event);
		} else if (event instanceof GetDataMessage) {
			onGetDataMessage(me, pid, (GetDataMessage) event);
		} else if (event instanceof BlockMessage) {
			onBlockMessage(me, pid, (BlockMessage) event);
		} else if (event instanceof TxMessage) {
			onTxMessage(me, pid, (TxMessage) event);
		}

		// events
		else if (event instanceof BlockFoundEvent) {
			onBlockFoundEvent(me, pid, (BlockFoundEvent) event);
		} else if (event instanceof StartEvent) {
			onStart(me, pid);
		}

		// no other events are possible
		else {
			assert false : "BitcoinCore got an unknown event: " + event;
		}
	}

	/**
	 * This method is invoked once the simulator hasBlock been bootstrap and is ready to run the simulation.
	 * We start to simulate the mining process here.
	 */
	private void onStart(Node me, int pid) {
		scheduleNextBlockMining(me, pid);
	}

	/**
	 * A new block hasBlock been discovered by this node. Please note that the {@link BlockFoundEvent}
	 * is always scheduled on the simulator when a new mining process is started, but the mining
	 * can be interrupted because some other node is faster than this one. This method will take
	 * care of discriminating real {@link BlockFoundEvent} from wrong ones.
	 */
	private void onBlockFoundEvent(Node me, int pid, BlockFoundEvent event) {
		if (event == lastBlockFoundEvent) {
			final Block block = Blocks.nextBlock(event.previous, event.transactions, me.getID(), CommonState.getTime());
			addToBlockchain(me, pid, block);
			gossipBlock(me, pid, block);
			scheduleNextBlockMining(me, pid);
		}
	}


	// ------------------------------------------------------------------------------
	//  Messages
	// ------------------------------------------------------------------------------

	/**
	 * Schedule the mining of a new block as an event in the simulator. This method schedules
	 * a {@link BlockFoundEvent} after an exponential time. Please note that the events in the
	 * simulator cannot be ignored, so we need to check that the mining hasBlock not been interrupted
	 * when receiving a{@link BlockFoundEvent}. See {@link #onBlockFoundEvent(Node, int, BlockFoundEvent)}.
	 */
	private void scheduleNextBlockMining(Node me, int pid) {
		final long delay = Distributions.roundedNextExponential(mean);
		final TransactionsWrapper transactions = selectNextTransactions();
		final BlockFoundEvent event = new BlockFoundEvent(CommonState.getTime(), blockchain.longestFork(), transactions);
		EDSimulator.add(delay, event, me, pid);
		lastBlockFoundEvent = event;
	}

	/**
	 * On a {@link GetBlocksMessage}, the node replies with the an {@link InvMessage}
	 * containing the list of requested block headers.
	 */
	private void onGetBlocksMessage(Node me, int pid, GetBlocksMessage message) {

		// query the blockchain and getBlock all descendants of the requested block hashes
		final List<Block> replyBlocks = new LinkedList<>();
		final int[] blockIDs = message.blockHeaders;
		for (int id : blockIDs) {
			final Block block = blockchain.getBlock(id);
			final List<Block> descendants = blockchain.descendants(block != null ? block : Blocks.GENESIS);
			replyBlocks.addAll(descendants);
		}

		// extract the block headers
		final int[] replyBlockHeaders = replyBlocks.stream().mapToInt(block -> block.id).toArray();

		// reply to the node with an Inv message
		final InvMessage invMessage = new InvMessage(me, Type.BLOCK, replyBlockHeaders);
		sendCheckLinkable(me, message.sender, pid, invMessage);
	}

	/**
	 * On a {@link InvMessage}, the node checks the received inventory against the
	 * local knowledge and request any missing block / transaction if needed.
	 */
	private void onInvMessage(Node me, int pid, InvMessage message) {
		switch (message.type) {

			// received blocks headers
			case BLOCK: {
				final IntQueue queue = new IntQueue();
				final int[] blockIDs = message.headers;
				for (int id : blockIDs) {
					if (!blockchain.hasBlock(id)) {
						queue.add(id);
					}
				}
				if (!queue.isEmpty()) {
					final GetDataMessage getDataMessage = new GetDataMessage(me, Type.BLOCK, queue);
					sendCheckLinkable(me, message.sender, pid, getDataMessage);
				}
				break;
			}

			// received transactions headers
			case TRANSACTION: {
				throw new RuntimeException("Transactions in an InvMessage are not yet supported");
			}

			// no other types are possible
			default: {
				assert false : "Bitcoin core got an InvMessage for an unknown type: " + message.type;
			}
		}
	}

	/**
	 * On a {@link GetDataMessage}, the node searches the requested objects and
	 * reply with one {@link BlockMessage} or {@link TxMessage} for each object found.
	 */
	private void onGetDataMessage(Node me, int pid, GetDataMessage message) {
		switch (message.type) {

			// requested blocks
			case BLOCK: {
				for (int i = 0; i < message.headers.length; i++) {
					final int id = message.headers.array[i];
					if (blockchain.hasBlock(id)) {
						final BlockMessage blockMessage = new BlockMessage(blockchain.getBlock(id));
						sendCheckLinkable(me, message.sender, pid, blockMessage);
					}
				}
				break;
			}

			// requested transactions
			case TRANSACTION: {
				throw new RuntimeException("Transactions in a GetDataMessage are not yet supported");
			}

			// no other types are possible
			default: {
				assert false : "Bitcoin core got an GetDataMessage for an unknown type: " + message.type;
			}
		}
	}

	/**
	 * On a {@link BlockMessage}, the node adds the received block to the local
	 * blockchain and restarts the mining from the longest fork, if needed.
	 */
	private void onBlockMessage(Node me, int pid, BlockMessage message) {
		addToBlockchain(me, pid, message.block);
		restartMiningIfNeeded(me, pid);
	}


	// ------------------------------------------------------------------------------
	//  Block Utilities
	// ------------------------------------------------------------------------------

	/**
	 * On a {@link TxMessage}, the node adds it to the list of transactions to process
	 * and broadcasts it to all other nodes, if not done yet.
	 */
	private void onTxMessage(Node me, int pid, TxMessage message) {
		knowTransactions.set(message.transaction.id, true);
		gossipTransaction(me, pid, message);
	}

	/**
	 * Select the transactions to insert in the new block from those that are
	 * known but not yet stored in the blockchain.
	 */
	private TransactionsWrapper selectNextTransactions() {
		final TransactionsWrapper wrapper = new TransactionsWrapper(maxBlockSize);
		int count = 0;
		int i = 0;
		while (count < maxBlockSize && i < knowTransactions.length()) {
			final boolean isKnown = knowTransactions.get(i);
			final boolean storedInBlocks = blockchain.hasProcessedTransactions(i);
			if (isKnown & !storedInBlocks) {
				final Transaction current = Transactions.getTransaction(i);
				wrapper.transactions[count] = current;
				count++;
			}
			i++;
		}
		wrapper.transactionsNumber = count;
		return wrapper;
	}

	/**
	 * Adds a new block to the blockchain. If the block is new, broadcast it to all peers.
	 * This method will take care of handling duplicates.
	 */
	private void addToBlockchain(Node me, int pid, Block block) {

		// try to addBlock the block to the blockchain...
		final boolean result = processBlock(me, pid, block);

		// managed to insert the block in the blockchain and gossiped it
		// now we can try to process the old blocks
		if (result) {
			boolean proceed = true;
			while (proceed && !blocksToProcess.empty()) {
				final Block next = blocksToProcess.head();
				proceed = processBlock(me, pid, next);
				if (proceed) {
					blocksToProcess.dequeue();
				}
			}
		}

		// parent of the given block is not available...
		// we request for the node and just queue the received block
		else {

			// temporary queue this block...
			blocksToProcess.enqueue(block);

			// ... and request its parent
			final GetBlocksMessage message = new GetBlocksMessage(me, new int[]{block.previous.id});
			broadcast(me, pid, message);
		}
	}

	private boolean processBlock(Node me, int pid, Block block) {

		// try to addBlock the block to the blockchain...
		// please note that this operation may fail if the parent node is missing
		// so we need to take extra care of requesting the peers for the missing blocks
		final boolean result = blockchain.addBlock(block);

		// the block can be insert in the blockchain
		// NB: the blockchain object keeps track of the processed transactions
		if (result) {

			// gossip block to the other nodes
			gossipBlock(me, pid, block);
		}

		return result;
	}

	/**
	 * Forward the given block to all neighbours if the block is new, otherwise do nothing.
	 * This is done to prevent to send the same block too many times.
	 */
	private void gossipBlock(Node me, int pid, Block block) {
		if (!gossipedBlocks.get(block.id)) {
			gossipedBlocks.set(block.id, true);
			broadcast(me, pid, new BlockMessage(block));
		}
	}

	/**
	 * Forward the given transaction to all neighbours if the block is new, otherwise do nothing.
	 * This is done to prevent to send the same block too many times.
	 */
	private void gossipTransaction(Node me, int pid, TxMessage message) {
		final int id = message.transaction.id;
		if (!gossipedTransactions.get(id)) {
			gossipedTransactions.set(id, true);
			broadcast(me, pid, message);
		}
	}


	// ------------------------------------------------------------------------------
	//  Network Utilities
	// ------------------------------------------------------------------------------

	/**
	 * If the node is not mining the longest chain anymore, stop the mining process
	 * and restart it from the new longest chain.
	 */
	@SuppressWarnings("StatementWithEmptyBody")
	private void restartMiningIfNeeded(Node me, int pid) {
		final Block longestChain = blockchain.longestFork();
		if (miningFromBlock == longestChain) {
			// no-op: we are already mining the longest chain...
		} else {
			// somebody discovered a block before me and changed the longest chain...
			// BitCoin strategy tells to always mine from the longest chain
			miningFromBlock = longestChain;
			scheduleNextBlockMining(me, pid);
		}
	}
}
