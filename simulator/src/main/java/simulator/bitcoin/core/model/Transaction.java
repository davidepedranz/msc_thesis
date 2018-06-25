package simulator.bitcoin.core.model;

/**
 * Models a single transaction in the Bitcoin protocol.
 */
@SuppressWarnings("WeakerAccess")
public final class Transaction {

	public final int id;
	public final long timestamp;

	Transaction(int id, long timestamp) {
		this.id = id;
		this.timestamp = timestamp;
	}

	@Override
	public String toString() {
		return "Transaction{" +
			"id=" + id +
			", timestamp=" + timestamp +
			'}';
	}
}
