package simulator.utilities;

import peersim.core.CommonState;
import simulator.model.Transaction;

import java.util.ArrayList;
import java.util.List;

/**
 * Static class that hold the common state between all nodes in the network.
 * This is classes stores only IMMUTABLE objects and is used to spare memory
 * during the simulations (only one instance for each object will exist, and
 * the nodes will just copy a reference to the object instances).
 */
public final class GlobalState {

	// TODO!
	private static int ADDRESSES_COUNTER = 20;

	// assign to each transaction a different progressive ID
	private static int TRANSACTION_COUNTER = 0;

	// TODO: replace with plain array?
	// keep track of all transactions, in order of generation
	private static final List<Transaction> TRANSACTIONS = new ArrayList<>();

	// TODO!
	public static int nextAddress() {
		ADDRESSES_COUNTER++;
		return ADDRESSES_COUNTER;
	}

	public static Transaction getTransaction(int i) {
		return TRANSACTIONS.get(i);
	}

	public static Transaction nextTransaction() {
		final Transaction transaction = Transaction.create(
			TRANSACTION_COUNTER++,
			CommonState.r.nextInt(ADDRESSES_COUNTER) + 1,        // use addresses from 1 to N
			CommonState.r.nextInt(ADDRESSES_COUNTER) + 1,          // use addresses from 1 to N
			CommonState.r.nextInt()
		);
		TRANSACTIONS.add(transaction);
		return transaction;
	}
}
