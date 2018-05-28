package simulator.model;

@SuppressWarnings("WeakerAccess")
public final class Block {

	// counter for blocks
	private static int BLOCK_ID;

	private static int nextBlock() {
		return BLOCK_ID++;
	}

	private static final int INITIAL_BLOCKS_STORAGE_SIZE;
	public static final Block GENESIS;
	static Block[] blocks;
	private static int n;

	static {
		INITIAL_BLOCKS_STORAGE_SIZE = 1000;
		GENESIS = new Block(0, 0, null, new Transaction[0], -1);
		_init();
	}

	public static void _init() {
		BLOCK_ID = 1;
		GENESIS.children = new Block[1];
		GENESIS.childrenNumber = 0;
		blocks = new Block[INITIAL_BLOCKS_STORAGE_SIZE];
		blocks[0] = GENESIS;
		n = 1;
	}

	public static Block create(Block previous, Transaction[] transactions, long miner) {
		final Block block = new Block(nextBlock(), previous.height + 1, previous, transactions, miner);
		updateChildren(previous, block);
		saveBlock(block);
		return block;
	}

	private static void saveBlock(Block block) {
		if (blocks.length == n) {
			final int oldSize = blocks.length;
			final int newSize = 2 * oldSize;
			final Block[] newBlocks = new Block[newSize];
			System.arraycopy(blocks, 0, newBlocks, 0, oldSize);
			blocks = newBlocks;
		}
		blocks[block.id] = block;
		n++;
	}

	private static void updateChildren(Block parent, Block child) {
		if (parent.children.length == parent.childrenNumber) {
			final int oldSize = parent.children.length;
			final int newSize = 2 * oldSize;
			final Block[] newChildren = new Block[newSize];
			System.arraycopy(parent.children, 0, newChildren, 0, oldSize);
			parent.children = newChildren;
		}
		parent.children[parent.childrenNumber] = child;
		parent.childrenNumber++;
	}

	// fields
	public final int id;
	public final int height;
	public final Block previous;
	public final Transaction[] transactions;
	public final long miner;

	public Block[] children;
	public int childrenNumber;

	private Block(int id, int height, Block previous, Transaction[] transactions, long miner) {
		this.id = id;
		this.height = height;
		this.previous = previous;
		this.transactions = transactions;
		this.miner = miner;
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
		return String.format("Block{id=%s, previous=%s, height=%s}", id, previousID, height);
	}
}
