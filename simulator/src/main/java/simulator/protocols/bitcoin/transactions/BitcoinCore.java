package simulator.protocols.bitcoin.transactions;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.edsim.EDSimulator;
import simulator.events.StartEvent;
import simulator.model.Block;
import simulator.model.Blockchain;
import simulator.model.Transaction;
import simulator.observers.BlockchainMetric;
import simulator.protocols.bitcoin.transactions.events.BlockFoundEvent;
import simulator.protocols.bitcoin.transactions.messages.*;
import simulator.utilities.Distributions;
import simulator.utilities.GlobalState;
import simulator.utilities.IntQueue;
import simulator.utilities.NetworkUtilities;

import java.util.BitSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static simulator.utilities.NetworkUtilities.broadcast;
import static simulator.utilities.NetworkUtilities.reply;

// TODO: no verification is done!
public final class BitcoinCore implements BlockchainMetric, EDProtocol {

	// parameters
	private static final String PARAMETER_MEAN = "mean";
	private static final String PARAMETER_BLOCK_SIZE = "block_size";

	// configuration
	private final int mean;
	private final int maxBlockSize;

	// status of the nodes
	private Blockchain blockchain;
	private BitSet transactionsToProcess;
	private BitSet processedTransactions;

	// TODO: optimization: keep track of the first transaction to process
	private int lastProcessedTransactionIndex;

	private Block miningFromBlock;
	private BlockFoundEvent lastBlockFoundEvent = null;

	private List<Block> blocksToProcess;

	// TODO: optimize
	private BitSet gossipedBlocks;
	private BitSet gossipedTransactions;

	@SuppressWarnings("unused")
	public BitcoinCore(String prefix) {
		this.mean = Configuration.getInt(prefix + "." + PARAMETER_MEAN);
		this.maxBlockSize = Configuration.getInt(prefix + "." + PARAMETER_BLOCK_SIZE);

		// status
		this.blockchain = new Blockchain(Block.GENESIS);
		this.transactionsToProcess = new BitSet();
		this.processedTransactions = new BitSet();
		this.lastProcessedTransactionIndex = -1;
		this.miningFromBlock = Block.GENESIS;

		// TODO: change data structure?
		this.blocksToProcess = new LinkedList<>();

		this.gossipedBlocks = new BitSet();
		this.gossipedTransactions = new BitSet();
	}

	@Override
	public Blockchain blockchain() {
		return blockchain;
	}

	@Override
	public Object clone() {
		try {
			// NB: Block and BlockFoundEvent are immutable, so we do NOT need to clone them!
			// NB: lastProcessedTransactionIndex is a primitive type, Object.clone() will take care of it!
			final BitcoinCore clone = (BitcoinCore) super.clone();
			clone.blockchain = blockchain.clone();
			clone.transactionsToProcess = (BitSet) transactionsToProcess.clone();
			clone.processedTransactions = (BitSet) processedTransactions.clone();
			return clone;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException("Clone not supported", e);
		}
	}

	@Override
	public void processEvent(Node node, int pid, Object event) {

		// message: `GetBlocks`
		if (event instanceof GetBlocksMessage) {
			onGetBlocksMessage(node, pid, (GetBlocksMessage) event);
		}

		// message: `Inv`
		else if (event instanceof InvMessage) {
			onInvMessage(node, pid, (InvMessage) event);
		}

		// message: `GetData`
		else if (event instanceof GetDataMessage) {
			onGetDataMessage(node, pid, (GetDataMessage) event);
		}

		// message: `Block`
		else if (event instanceof BlockMessage) {
			onBlockMessage(node, pid, (BlockMessage) event);
		}

		// message: `TxMessage`
		else if (event instanceof TxMessage) {
			onTxMessage(node, pid, (TxMessage) event);
		}

		// event: block found
		else if (event instanceof BlockFoundEvent) {
			onBlockFoundEvent(node, pid, (BlockFoundEvent) event);
		}

		// event: start the protocol
		else if (event instanceof StartEvent) {
			onStart(node, pid);
		}

		// no other events are possible
		else {
			assert false : "BitcoinCore got an unknown event: " + event;
		}
	}


	// ------------------------------------------------------------------------------
	//  Events
	// ------------------------------------------------------------------------------

	private void onStart(Node node, int pid) {
		scheduleNextBlockMining(node, pid);
	}

	private void onBlockFoundEvent(Node node, int pid, BlockFoundEvent event) {
		if (event == lastBlockFoundEvent) {
			final Block block = event.block();
			addToBlockchain(node, pid, block);
			gossipBlock(node, pid, block);
			scheduleNextBlockMining(node, pid);
		}
	}


	// ------------------------------------------------------------------------------
	//  Messages
	// ------------------------------------------------------------------------------

	/**
	 * On a {@link GetBlocksMessage}, the node replies with the an {@link InvMessage}
	 * containing the list of requested block headers.
	 */
	private void onGetBlocksMessage(Node node, int pid, GetBlocksMessage message) {

		// query the blockchain and get all descendants of the requested block hashes
		final List<Block> replyBlocks = new LinkedList<>();
		final int[] blockIDs = message.blockHeaders;
		for (int id : blockIDs) {
			final Block block = blockchain.get(id);
			final List<Block> descendants = blockchain.descendants(block != null ? block : Block.GENESIS);
			replyBlocks.addAll(descendants);
		}

		// extract the block headers
		final int[] replyBlockHeaders = replyBlocks.stream().mapToInt(block -> block.id).toArray();

		// reply to the node with an Inv message
		final InvMessage invMessage = new InvMessage(node, Type.BLOCK, replyBlockHeaders);
		NetworkUtilities.reply(node, message.sender, pid, invMessage);
	}

	/**
	 * On a {@link InvMessage}, the node checks the received inventory against the
	 * local knowledge and request any missing block / transaction if needed.
	 */
	private void onInvMessage(Node node, int pid, InvMessage message) {
		switch (message.type) {

			// received blocks headers
			case BLOCK: {
				final IntQueue queue = new IntQueue();
				final int[] blockIDs = message.headers;
				for (int id : blockIDs) {
					if (!blockchain.has(id)) {
						queue.add(id);
					}
				}
				if (!queue.isEmpty()) {
					final GetDataMessage getDataMessage = new GetDataMessage(node, Type.BLOCK, queue);
					reply(node, message.sender, pid, getDataMessage);
				}
				break;
			}

			// received transactions headers
			case TRANSACTION: {
				// TODO: #ask_montresor
				break;
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
	private void onGetDataMessage(Node node, int pid, GetDataMessage message) {
		switch (message.type) {

			// requested blocks
			case BLOCK: {
				for (int i = 0; i < message.headers.length; i++) {
					final int id = message.headers.array[i];
					if (blockchain.has(id)) {
						final BlockMessage blockMessage = new BlockMessage(blockchain.get(id));
						reply(node, message.sender, pid, blockMessage);
					}
				}
				break;
			}

			// requested transactions
			case TRANSACTION: {
				// TODO: #ask_montresor
				break;
			}

			// no other types are possible
			default: {
				assert false : "Bitcoin core got an InvMessage for an unknown type: " + message.type;
			}
		}
	}

	/**
	 * On a {@link BlockMessage}, the node adds the received block to the local
	 * blockchain and restarts the mining from the longest fork, if needed.
	 */
	private void onBlockMessage(Node node, int pid, BlockMessage message) {
		addToBlockchain(node, pid, message.block);
		restartMiningIfNeeded(node, pid);
	}

	/**
	 * On a {@link TxMessage}, the node adds it to the list of transactions to process
	 * and broadcasts it to all other nodes, if not done yet.
	 */
	private void onTxMessage(Node node, int pid, TxMessage message) {
		transactionsToProcess.set(message.transaction.id(), true);
		gossipTransaction(node, pid, message);
	}


	// ------------------------------------------------------------------------------
	//  Utilities
	// ------------------------------------------------------------------------------

	/**
	 * Schedule the mining of a new block as an event in the simulator.
	 * This method schedules a {@link BlockFoundEvent} after an exponential time.
	 * Please note that the events in the simulator cannot be ignored, so we
	 * need to check that the mining has not been interrupted when receiving a
	 * {@link BlockFoundEvent}. See {@link #onBlockFoundEvent(Node, int, BlockFoundEvent)}.
	 */
	private void scheduleNextBlockMining(Node node, int pid) {
		final long delay = Distributions.roundedNextExponential(mean);
		final Block block = generateBlock(node.getID());
		final BlockFoundEvent event = BlockFoundEvent.create(CommonState.getTime(), block);   // TODO: create block only when needed
		EDSimulator.add(delay, event, node, pid);
		lastBlockFoundEvent = event;
	}

	private Block generateBlock(long miner) {
		final List<Transaction> transactions = new LinkedList<>();
		int count = 0;
		int i = lastProcessedTransactionIndex + 1;
		while (count < maxBlockSize && i < transactionsToProcess.length()) {
			if (!processedTransactions.get(i)) {
				final Transaction current = GlobalState.getTransaction(i);
				transactions.add(current);
				count++;
			}
			i++;
		}
		final Transaction[] array = new Transaction[transactions.size()];
		return Block.create(blockchain.longestChain(), transactions.toArray(array), miner);
	}

	@SuppressWarnings("StatementWithEmptyBody")
	private void restartMiningIfNeeded(Node node, int pid) {
		final Block longestChain = blockchain.longestChain();
		if (miningFromBlock == longestChain) {
			// no-op: we are already mining the longest chain...
		} else {
			// somebody discovered a block before me and changed the longest chain...
			// BitCoin strategy tells to always mine from the longest chain
			miningFromBlock = longestChain;
			scheduleNextBlockMining(node, pid);
		}
	}

	/**
	 * Adds a new block to the blockchain. If the block is new, broadcast it to all peers.
	 * This method will take care of handling duplicates.
	 */
	private void addToBlockchain(Node node, int pid, Block block) {

		// try to add the block to the blockchain...
		final boolean result = processBlock(node, pid, block);

		// managed to insert the block in the blockchain and gossiped it
		// now we can try to process the old blocks
		if (result) {
			boolean proceed = true;
			final Iterator<Block> iterator = blocksToProcess.iterator();
			while (proceed && iterator.hasNext()) {
				final Block next = iterator.next();
				proceed = processBlock(node, pid, next);
				if (proceed) {
					iterator.remove();
				}
			}
		}

		// parent of the given block is not available...
		// we request for the node and just queue the received block
		else {

			// temporary queue this block...
			blocksToProcess.add(block);

			// ... and request its parent
			final GetBlocksMessage message = new GetBlocksMessage(node, new int[]{block.previous.id});
			broadcast(node, pid, message);
			// TODO: here we request only a single block, since we miss only it... #ask_montresor
			// TODO: we can also shortcut this!!! ... and ask block immediately!
		}
	}

	private boolean processBlock(Node node, int pid, Block block) {

		// try to add the block to the blockchain...
		// please note that this operation may fail if the parent node is missing
		// so we need to take extra care of requesting the peers for the missing blocks
		final boolean result = blockchain.add(block);

		// the block can be insert in the blockchain
		if (result) {

			// mark the transactions as done
			for (Transaction transaction : block.transactions) {
				final int id = transaction.id();
				processedTransactions.set(id, true);

				// TODO: update lastProcessedTransactionIndex
				//			if (processedTransactions.get(blockID - 1)) {
				//				lastProcessedTransactionIndex = blockID;
				//			}
			}

			// gossip block to the other nodes
			gossipBlock(node, pid, block);
		}

		return result;
	}

	private void gossipBlock(Node node, int pid, Block block) {
		if (!gossipedBlocks.get(block.id)) {
			gossipedBlocks.set(block.id, true);
			broadcast(node, pid, new BlockMessage(block));
		}
	}

	private void gossipTransaction(Node node, int pid, TxMessage message) {
		final int id = message.transaction.id();
		if (!gossipedTransactions.get(id)) {
			gossipedTransactions.set(id, true);
			broadcast(node, pid, message);
		}
	}
}
