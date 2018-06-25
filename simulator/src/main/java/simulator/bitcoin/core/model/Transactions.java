package simulator.bitcoin.core.model;

import simulator.bitcoin.core.initializers.TransactionsInitializer;

import java.util.ArrayList;
import java.util.List;

/**
 * Global storage all ALL generated transactions. This class is populated at the beginning
 * for the simulation by {@link TransactionsInitializer}. The store
 * contains all transactions, even before they are scheduled to the nodes.
 */
public final class Transactions {

	// keep track of all transactions, in order of generation
	public static final List<Transaction> transactions = new ArrayList<>();

	// assign to each transaction a different progressive ID
	private static int transactionCounter = 0;

	/**
	 * Get the transaction with id i.
	 *
	 * @param i ID of the transaction to getBlock.
	 * @return Transaction with the given index.
	 */
	public static Transaction getTransaction(int i) {
		return transactions.get(i);
	}

	/**
	 * Generate a new transaction with the given timestamp and adds it to the store.
	 *
	 * @param timestamp Transaction creation timestamp.
	 * @return Newly generated transaction.
	 */
	public static Transaction nextTransaction(long timestamp) {
		final Transaction transaction = new Transaction(transactionCounter++, timestamp);
		transactions.add(transaction);
		return transaction;
	}
}
