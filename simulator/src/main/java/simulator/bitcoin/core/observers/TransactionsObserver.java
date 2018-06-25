package simulator.bitcoin.core.observers;

import peersim.core.CommonState;
import peersim.core.Control;
import peersim.util.IncrementalStats;
import simulator.bitcoin.core.model.*;

import java.util.function.BiFunction;

import static simulator.utilities.structures.ArrayUtilities.binarySearch;

/**
 * Observer that measures the time needed to store a transaction on the main branch
 * of the blockchain. NB: the same transaction can be in more than one chain.
 */
public final class TransactionsObserver implements Control {

	// fields
	private final String name;

	public TransactionsObserver(String name) {
		this.name = name;
	}

	@Override
	public boolean execute() {
		final IncrementalStats stats = new IncrementalStats();

		// find the index of the last transaction seen so far
		final BiFunction<Transaction, Long, Boolean> comparator = (tx, timestamp) -> tx.timestamp <= timestamp;
		final int lastTransactionIndex = binarySearch(Transactions.transactions, CommonState.getTime(), comparator);

		// nothing to compute if there are no transactions issued before the maximum timestamp
		if (lastTransactionIndex >= 0) {

			// compute the time needed to enter a block in the blockchain
			final long[] times = new long[lastTransactionIndex + 1];
			Block block = Blocks.longestChain;
			while (block != null) {
				final TransactionsWrapper wrapper = block.transactions;
				for (int i = 0; i < wrapper.transactionsNumber; i++) {
					final Transaction transaction = wrapper.transactions[i];
					final long delta = block.timestamp - transaction.timestamp;
					times[transaction.id] = delta;
				}
				block = block.previous;
			}

			// compute the statistics over all transactions generated so far...
			for (long time : times) {
				// TODO: we do not count the transactions that are not yet present in one block!!!
				// assert time > 0 : "Found a transaction with time-to-blockchain equals to zero.";
				if (time > 0) {
					stats.add(time);
				}
			}
		}

		// print them out, following Peersim conventions
		System.out.println(name + ": [" + CommonState.getTime() + "] " + stats);

		// false == do NOT stop the simulation
		return false;
	}
}
