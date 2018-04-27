package simulator.protocols;

import peersim.config.Configuration;
import peersim.config.FastConfig;
import peersim.core.CommonState;
import peersim.core.Linkable;
import peersim.core.Network;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.edsim.EDSimulator;
import peersim.transport.Transport;
import simulator.events.BlockFoundEvent;
import simulator.events.StartEvent;
import simulator.model.Block;
import simulator.model.Transaction;
import simulator.observers.ForksMetric;
import simulator.utilities.GlobalState;

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
	private final BitcoinStatus[] statuses;

	@SuppressWarnings("unused")
	public BitcoinProtocol(String prefix) {
		this.mean = Configuration.getInt(prefix + "." + PARAMETER_MEAN);
		this.maxBlockSize = Configuration.getInt(prefix + "." + PARAMETER_BLOCK_SIZE);

		final int size = Network.size();
		this.statuses = new BitcoinStatus[size];
		for (int i = 0; i < size; i++) {
			statuses[i] = new BitcoinStatus();
		}
	}

	@Override
	public long forks(int nodeIndex) {
		return statuses[nodeIndex].blockchain.forks();
	}

	// TODO: read Java docs...
	@Override
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException("Clone not supported", e);
		}
	}

	@Override
	public void processEvent(Node node, int pid, Object event) {

		// extract the status of the given node
		final BitcoinStatus status = status(node);

		// start the protocol
		if (event instanceof StartEvent) {
			scheduleNextBlock(status, node, pid);
		}

		// got a transaction... add to the list of known transactions
		else if (event instanceof Transaction) {
			addTransaction(status, (Transaction) event);
		}

		// new block... add to the blockchain and stop mining if needed
		else if (event instanceof Block) {
			final Block block = (Block) event;
			addBlockToChain(status, block);
			restartMiningIfNeeded(status, node, pid);
		}

		// I managed to mine a block
		else if (event instanceof BlockFoundEvent) {
			blockFound(status, node, pid, (BlockFoundEvent) event);
		}
	}

	private BitcoinStatus status(Node node) {
		return statuses[node.getIndex()];
	}

	private void addTransaction(BitcoinStatus status, Transaction transaction) {
		status.transactionsToProcess.set(transaction.id(), true);
	}

	private void addBlockToChain(BitcoinStatus status, Block block) {
		status.blockchain.add(block);

		for (Transaction transaction : block.transactions()) {
			final int id = transaction.id();
			status.processedTransactions.set(id, true);

			// TODO: update lastProcessedTransactionIndex
			//			if (processedTransactions.get(id - 1)) {
			//				lastProcessedTransactionIndex = id;
			//			}
		}
	}

	@SuppressWarnings("StatementWithEmptyBody")
	private void restartMiningIfNeeded(BitcoinStatus status, Node node, int pid) {
		final Block longestChain = status.blockchain.longestChain();
		if (status.miningFromBlock == longestChain) {
			// no-op: we are already mining the longest chain...
		} else {
			// somebody discovered a block before me and changed the longest chain...
			// BitCoin strategy tells to always mine from the longest chain
			status.miningFromBlock = longestChain;
			scheduleNextBlock(status, node, pid);
		}
	}

	private void scheduleNextBlock(BitcoinStatus status, Node node, int pid) {
		final long delay = CommonState.r.nextPoisson(mean);
		final Block block = generateBlock(status, node.getID());
		final BlockFoundEvent event = BlockFoundEvent.create(CommonState.getTime(), block);
		EDSimulator.add(delay, event, node, pid);
		status.lastBlockFoundEvent = event;
	}

	private Block generateBlock(BitcoinStatus status, long miner) {
		final List<Transaction> transactions = new LinkedList<>();
		int count = 0;
		int i = status.lastProcessedTransactionIndex + 1;
		while (count < maxBlockSize && i < status.transactionsToProcess.length()) {
			if (!status.processedTransactions.get(i)) {
				final Transaction current = GlobalState.getTransaction(i);
				transactions.add(current);
				count++;
			}
			i++;
		}
		return Block.create(status.blockchain.longestChain(), transactions, miner);
	}

	private void publishBlock(Node node, int pid, Block block) {
		final Linkable linkable = (Linkable) node.getProtocol(FastConfig.getLinkable(pid));
		final Transport transport = (Transport) node.getProtocol(FastConfig.getTransport(pid));
		for (int i = 0; i < linkable.degree(); i++) {

			// TODO: better way not to send to myself
			if (node.getIndex() != i) {
				transport.send(node, linkable.getNeighbor(i), block, pid);
			}
		}
	}

	private void blockFound(BitcoinStatus status, Node node, int pid, BlockFoundEvent event) {
		if (event == status.lastBlockFoundEvent) {
			final Block block = event.block();
			addBlockToChain(status, block);
			publishBlock(node, pid, block);
			scheduleNextBlock(status, node, pid);
		}
	}
}
