package simulator.bitcoin.core.model;

/**
 * Models a single block in the Bitcoin protocol.
 */
@SuppressWarnings("WeakerAccess")
public final class Block {

	// fields
	public final int id;
	public final int height;
	public final Block previous;
	public final TransactionsWrapper transactions;
	public final long miner;
	public final long timestamp;
	public Block[] children;
	public int childrenNumber;

	Block(int id, int height, Block previous, TransactionsWrapper transactions, long miner, long timestamp) {
		this.id = id;
		this.height = height;
		this.previous = previous;
		this.transactions = transactions;
		this.miner = miner;
		this.timestamp = timestamp;
		this.children = new Block[1];
		this.childrenNumber = 0;
	}

	@Override
	public int hashCode() {
		return id;
	}

	@Override
	public boolean equals(Object o) {
		assert o instanceof Block;
		return id == ((Block) o).id;
	}

	@Override
	public String toString() {
		final Integer previousID = previous != null ? previous.id : null;
		return String.format("Block{id=%s, previous=%s, height=%s, timestamp=%s}", id, previousID, height, timestamp);
	}
}
