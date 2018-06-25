package simulator.bitcoin.core.model;

/**
 * Container of transactions that fit in one {@link Block}.
 * This class is used for performances reasons. It always allocate the maximum number of
 * transactions that can be contained in a single Block and keeps count of the real number.
 */
@SuppressWarnings("WeakerAccess")
public final class TransactionsWrapper {

	public final Transaction[] transactions;
	public int transactionsNumber;

	public TransactionsWrapper(int blockSize) {
		this.transactions = new Transaction[blockSize];
		this.transactionsNumber = 0;
	}

	public TransactionsWrapper(Transaction[] transactions) {
		this.transactions = transactions;
		this.transactionsNumber = transactions.length;
	}
}
