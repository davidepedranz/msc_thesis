package simulator.protocols;

import simulator.events.BlockFoundEvent;
import simulator.model.Block;
import simulator.model.Blockchain;

import java.util.BitSet;

/**
 * Class that contains the entire status of a node in the Bitcoin protocol.
 */
class BitcoinStatus {

	final Blockchain blockchain;
	final BitSet transactionsToProcess;
	final BitSet processedTransactions;

	// TODO: optimization: keep track of the first transaction to process
	int lastProcessedTransactionIndex;

	Block miningFromBlock;
	BlockFoundEvent lastBlockFoundEvent = null;

	BitcoinStatus() {
		this.blockchain = new Blockchain(Block.GENESIS);
		this.transactionsToProcess = new BitSet();
		this.processedTransactions = new BitSet();
		this.lastProcessedTransactionIndex = -1;

		this.miningFromBlock = Block.GENESIS;
	}
}
