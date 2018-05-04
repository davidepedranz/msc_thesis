package simulator.model;

@SuppressWarnings("WeakerAccess")
public final class Block {

	// counter for blocks
	private static int BLOCK_ID = 0;

	private static int nextBlock() {
		return BLOCK_ID++;
	}

	/**
	 * Special block that is the root of each blockchain.
	 */
	public static Block GENESIS = new Block(nextBlock(), 0, null, new Transaction[0], -1);

	public static Block create(Block previous, Transaction[] transactions, long miner) {
		return new Block(nextBlock(), previous.depth + 1, previous, transactions, miner);
	}

	// fields
	public final int id;
	public final int depth;
	public final Block previous;
	public final Transaction[] transactions;
	public final long miner;

	public Block(int id, int depth, Block previous, Transaction[] transactions, long miner) {
		this.id = id;
		this.depth = depth;
		this.previous = previous;
		this.transactions = transactions;
		this.miner = miner;
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
		return String.format("Block{id=%s, previous=%s, depth=%s}", id, previousID, depth);
	}
}
