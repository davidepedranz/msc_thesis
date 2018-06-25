package simulator.bitcoin.core.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Global storage all ALL generated blocks. The store is populated when blocks
 * are generated during the simulation. Blocks are only generated when the mining
 * finishes, so this class will store only correct blocks.
 */
public final class Blocks {

	// keep track of the Genesis block
	public static final Block GENESIS = new Block(0, 0, null, new TransactionsWrapper(0), -1, 0);

	// keep track of all blocks, in order of generation
	private static final List<Block> blocks = new ArrayList<>();

	// keep track of the longest chain
	public static Block longestChain;

	// assign to each block a different progressive ID
	private static int blocksCounter;

	static {
		_init();
	}

	/**
	 * Reset the state of the blocks to the original one (only the genesis exists).
	 */
	public static void _init() {
		blocksCounter = 1;
		longestChain = GENESIS;
		GENESIS.children = new Block[1];
		GENESIS.childrenNumber = 0;
		blocks.add(GENESIS);
	}

	/**
	 * Get the block with id i.
	 *
	 * @param i ID of the block to getBlock.
	 * @return Block with the given index.
	 */
	static Block getBlock(int i) {
		return blocks.get(i);
	}

	/**
	 * Generate a new block with the given transactions.
	 *
	 * @param previous     Previous block.
	 * @param transactions List of transactions for the block.
	 * @param miner        ID of the miner that generated this block.
	 * @param timestamp    Creation timestamp.
	 * @return The newly created block.
	 */
	public static Block nextBlock(Block previous, TransactionsWrapper transactions, long miner, long timestamp) {
		final Block block = new Block(blocksCounter++, previous.height + 1, previous, transactions, miner, timestamp);
		updateChildren(previous, block);
		updateLongestChain(block);
		blocks.add(block);
		return block;
	}

	/**
	 * Blocks store double pointers to the parent and the children nodes.
	 * This method updated the pointers on creation of a new child.
	 *
	 * @param parent Parent node.
	 * @param child  Child node.
	 */
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

	/**
	 * Keep track of the block with the greatest height.
	 *
	 * @param block Newly generated block.
	 */
	private static void updateLongestChain(Block block) {
		if (block.height > longestChain.height) {
			longestChain = block;
		}
	}
}
