package simulator.protocols.bitcoin.transactions;

import peersim.config.Configuration;
import peersim.config.FastConfig;
import peersim.core.CommonState;
import peersim.core.Linkable;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.edsim.EDSimulator;
import peersim.transport.Transport;
import simulator.events.StartEvent;
import simulator.model.Block;
import simulator.model.Blockchain;
import simulator.model.Transaction;
import simulator.observers.ForksMetric;
import simulator.utilities.Distributions;
import simulator.utilities.GlobalState;

import java.util.BitSet;
import java.util.LinkedList;
import java.util.List;

/**
 * Simplified version of Bitcoin:
 * - tracks the blockchain view of each node
 * - simulated Proof-of-Work
 * - assumes transactions are immediately available to the entire network
 * - assumes all blocks are VALID (TODO: handle attacks?)
 */
public final class BitcoinProtocol implements ForksMetric, EDProtocol {

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

	@SuppressWarnings("unused")
	public BitcoinProtocol(String prefix) {
		this.mean = Configuration.getInt(prefix + "." + PARAMETER_MEAN);
		this.maxBlockSize = Configuration.getInt(prefix + "." + PARAMETER_BLOCK_SIZE);

		// status
		this.blockchain = new Blockchain(Block.GENESIS);
		this.transactionsToProcess = new BitSet();
		this.processedTransactions = new BitSet();
		this.lastProcessedTransactionIndex = -1;
		this.miningFromBlock = Block.GENESIS;
	}

	@Override
	public long forks(int nodeIndex) {
		return blockchain.forks();
	}

	@Override
	public Object clone() {
		try {
			// NB: Block and BlockFoundEvent are immutable, so we do NOT need to clone them!
			// NB: lastProcessedTransactionIndex is a primitive type, Object.clone() will take care of it!
			final BitcoinProtocol clone = (BitcoinProtocol) super.clone();
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

		// got a transaction... add to the list of known transactions
		if (event instanceof Transaction) {
			addTransaction((Transaction) event);
		}

		// new block... add to the blockchain and stop mining if needed
		else if (event instanceof Block) {
			final Block block = (Block) event;
			addBlockToChain(block);
			restartMiningIfNeeded(node, pid);
		}

		// I managed to mine a block
		else if (event instanceof BlockFoundEvent) {
			blockFound(node, pid, (BlockFoundEvent) event);
		}

		// start the protocol
		else if (event instanceof StartEvent) {
			scheduleNextBlock(node, pid);
		}

		// no other events are possible
		else {
			assert false : "BitcoinProtocol got an unknown event: " + event;
		}
	}

	private void addTransaction(Transaction transaction) {
		transactionsToProcess.set(transaction.id(), true);
	}

	private void addBlockToChain(Block block) {
		blockchain.add(block);

		for (Transaction transaction : block.transactions()) {
			final int id = transaction.id();
			processedTransactions.set(id, true);

			// TODO: update lastProcessedTransactionIndex
			//			if (processedTransactions.get(id - 1)) {
			//				lastProcessedTransactionIndex = id;
			//			}
		}
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
			scheduleNextBlock(node, pid);
		}
	}

	private void scheduleNextBlock(Node node, int pid) {
		final long delay = Distributions.roundedNextExponential(mean);
		final Block block = generateBlock(node.getID());
		final BlockFoundEvent event = BlockFoundEvent.create(CommonState.getTime(), block);
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
		return Block.create(blockchain.longestChain(), transactions, miner);
	}

	private void publishBlock(Node node, int pid, Block block) {
		final Linkable linkable = (Linkable) node.getProtocol(FastConfig.getLinkable(pid));
		final Transport transport = (Transport) node.getProtocol(FastConfig.getTransport(pid));
		for (int i = 0; i < linkable.degree(); i++) {

			// TODO: better way not to send to myself ... maybe move to Linkable?
			if (node.getIndex() != i) {
				transport.send(node, linkable.getNeighbor(i), block, pid);
			}
		}
	}

	private void blockFound(Node node, int pid, BlockFoundEvent event) {
		if (event == lastBlockFoundEvent) {
			final Block block = event.block();
			addBlockToChain(block);
			publishBlock(node, pid, block);
			scheduleNextBlock(node, pid);
		}
	}
}
