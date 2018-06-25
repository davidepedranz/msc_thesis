package simulator.bitcoin.core.model;

import java.util.Arrays;
import java.util.BitSet;
import java.util.LinkedList;
import java.util.List;

/**
 * Data-structure that simulates a Blockchain, like the one used in Bitcoin.
 * Please note that each simulates node hasBlock its own Blockchain object since it
 * could have a vision different from the other nodes.
 */
public final class Blockchain {
	private static final int DEFAULT_INITIAL_CAPACITY = 50;

	// internal status
	private Block[] forks;
	private int[] forksDepths;
	private int currentForks;
	private int longestForkIndex;
	private BitSet seenBlocks;
	private BitSet processedTransactions;

	/**
	 * Construct a new blockchain using the given block as genesis.
	 *
	 * @param genesis Genesis block.
	 */
	public Blockchain(Block genesis) {
		this.forks = new Block[DEFAULT_INITIAL_CAPACITY];
		this.forks[0] = genesis;
		this.forksDepths = new int[DEFAULT_INITIAL_CAPACITY];
		this.forksDepths[0] = 0;
		this.currentForks = 1;
		this.longestForkIndex = 0;
		this.seenBlocks = new BitSet();
		this.seenBlocks.set(genesis.id);
		this.processedTransactions = new BitSet();
	}

	/**
	 * Copy-constructor.
	 *
	 * @param original Original object to copy.
	 */
	public Blockchain(Blockchain original) {
		this.forks = new Block[original.forks.length];
		System.arraycopy(original.forks, 0, this.forks, 0, original.forks.length);
		this.forksDepths = new int[original.forksDepths.length];
		System.arraycopy(original.forksDepths, 0, this.forksDepths, 0, original.forksDepths.length);
		this.currentForks = original.currentForks;
		this.longestForkIndex = original.longestForkIndex;
		this.seenBlocks = (BitSet) original.seenBlocks.clone();
		this.processedTransactions = (BitSet) original.processedTransactions.clone();
	}

	/**
	 * @return The last block of the longest branch.
	 */
	public Block longestFork() {
		return forks[longestForkIndex];
	}

	/**
	 * @return Number of forks of the blockchain.
	 */
	long forksNumber() {
		return currentForks;
	}

	/**
	 * Check if the longest chain contains the transaction with the given ID.
	 *
	 * @param transactionId ID of the transaction to check.
	 * @return True if the longest chain contains the given transaction, false otherwise.
	 */
	public boolean hasProcessedTransactions(int transactionId) {
		return processedTransactions.get(transactionId);
	}

	/**
	 * Check if the block with the given ID is present in the blockchain.
	 *
	 * @param blockId ID of the block.
	 * @return True if the block is present in the blockchain, false otherwise.
	 */
	public boolean hasBlock(int blockId) {
		return seenBlocks.get(blockId);
	}

	/**
	 * Get the block with the given ID if present, null otherwise.
	 *
	 * @param blockId ID of the block.
	 * @return Block with the given ID if present, null otherwise.
	 */
	public Block getBlock(int blockId) {
		if (seenBlocks.get(blockId)) {
			return Blocks.getBlock(blockId);
		} else {
			return null;
		}
	}

	/**
	 * Add a new block to the blockchain. This method will recompute the forks
	 * and processed transactions depending on the added block (continuation of
	 * the longest chain, new fork, etc).
	 *
	 * @param block Block to add to the blockchain.
	 * @return True if the block was added, false if the block could not be added
	 * (eg. it was already present of one ancestor is missing).
	 */
	public boolean addBlock(Block block) {

		// check that I actually have seen the parent
		if (!seenBlocks.get(block.previous.id)) {
			return false;
		}

		// if I have seen the parent, I also saw this block, so I can addBlock it
		else {
			seenBlocks.set(block.id);
		}

		// try to find the parent of this node (this is not a new fork)
		for (int i = 0; i < currentForks; i++) {

			// handle duplicates (I may receive the same message multiple times in a gossip protocol)
			if (forks[i] == block) {
				// duplicate, skip block!
				return true;
			}

			// parent found
			if (forks[i] == block.previous) {

				// we addBlock a block on top of another one...
				// ... so we remove the current one and replace it with the new block
				// this is NOT a new fork!
				forks[i] = block;

				// maybe we need to recompute the distances from the longest chain...
				final boolean extendingLongestChain = i == longestForkIndex;
				if (extendingLongestChain) {

					// do nothing, we replaced already the longest chain
					// the distances of the forks from the common parent with the longest chain do not change

					// since we are on the longest chain, we can mark the transactions as done
					final TransactionsWrapper wrapper = block.transactions;
					for (int t = 0; t < wrapper.transactionsNumber; t++) {
						processedTransactions.set(wrapper.transactions[t].id, true);
					}

				} else {

					// this fork is now the longest chain!
					if (block.height > longestFork().height) {
						longestForkIndex = i;
						recomputeDepths();
						recomputeTransactions(longestFork(), block);
					}

					// this fork is not the longest chain
					// we do NOT need to flag the transactions as done...
					else {
						// we are now a step further away from the longest chain
						forksDepths[i]++;
					}
				}

				// optimization: only one block can be the parent, exit the loop
				return true;
			}
		}

		// make sure we have enough space in the array
		expandArrayIfNeeded();

		// if here, we have a new fork
		final int newIndex = currentForks;
		forks[currentForks] = block;
		currentForks++;

		// a new fork cannot be the longest chain, not yet!
		assert block.height <= longestFork().height : "a new fork cannot be the longest chain";
		forksDepths[newIndex] = computeDepth(block, longestFork());

		// in both cases, the block was added successfully
		return true;
	}

	/**
	 * Update the depths of all forks based on the new status of the blockchain (eg. new longest chain).
	 */
	private void recomputeDepths() {
		final Block longestChain = forks[longestForkIndex];
		for (int i = 0; i < currentForks; i++) {
			forksDepths[i] = computeDepth(forks[i], longestChain);
		}
	}

	/**
	 * Recompute the depth of a given fork with respect to the longest chain.
	 *
	 * @param block        Block to calculate the depth of.
	 * @param longestChain Last block of the longest chain in the blockchain.
	 * @return Depth of the given fork wrt the longest chain.
	 */
	private int computeDepth(Block block, Block longestChain) {
		int steps = 0;

		// base case: we got to the same block
		if (block == longestChain) {
			return steps;
		}

		// getBlock to the parent of the longest longest chain at the same height of the other block
		final int currentNodeDepth = block.height;
		while (longestChain.height > currentNodeDepth) {
			longestChain = longestChain.previous;
		}

		// go on step by step, till a match is found
		while (block != longestChain) {
			block = block.previous;
			longestChain = longestChain.previous;
			steps++;
		}

		return steps;
	}

	/**
	 * Compute which transactions needs to be processed after that a new fork got longer
	 * than the old longest one.
	 *
	 * @param oldLongest Old longest fork.
	 * @param newLongest New longest fork.
	 */
	private void recomputeTransactions(Block oldLongest, Block newLongest) {

		// find the common ancestor, since it is the fork point
		final Block commonAncestor = findCommonAncestor(oldLongest, newLongest);

		// un-flag transactions from the ex-longest chain
		while (oldLongest != commonAncestor) {
			final TransactionsWrapper wrapper = oldLongest.transactions;
			for (int i = 0; i < wrapper.transactionsNumber; i++) {
				final Transaction transaction = wrapper.transactions[i];
				processedTransactions.set(transaction.id, false);
			}
		}

		// flag transactions from the new longest chain
		while (newLongest != commonAncestor) {
			final TransactionsWrapper wrapper = newLongest.transactions;
			for (int i = 0; i < wrapper.transactionsNumber; i++) {
				final Transaction transaction = wrapper.transactions[i];
				processedTransactions.set(transaction.id, true);
			}
		}
	}

	/**
	 * Find the first common ancestor between two blocks in the blockchain.
	 *
	 * @param shortest Block on the shortest of the two chains.
	 * @param longest  Block on the longest of the two chains.
	 * @return The first common ancestor.
	 */
	Block findCommonAncestor(Block shortest, Block longest) {
		assert longest.height >= shortest.height;

		// getBlock the longest to the same level as the shortest
		while (longest.height > shortest.height) {
			longest = longest.previous;
		}

		// proceed one step at a time until we getBlock to the same block, i.e. the first common ancestor
		while (shortest != longest) {
			shortest = shortest.previous;
			longest = longest.previous;
		}

		// return one of the two, they are the same
		return shortest;
	}

	/**
	 * Find all blocks that are descendants of the given one.
	 *
	 * @param block Block.
	 * @return Descendants of the given block.
	 */
	public List<Block> descendants(Block block) {

		// initialization
		final LinkedList<Block> descendants = new LinkedList<>();
		final LinkedList<Block> toVisit = new LinkedList<>();
		toVisit.add(block);

		// visit
		Block current;
		while ((current = toVisit.pollFirst()) != null) {
			toVisit.addAll(Arrays.asList(current.children).subList(0, current.childrenNumber));
			descendants.add(current);
		}

		return descendants;
	}


	// TODO: do I really need to copy this?
	Block[] forks() {
		final Block[] tmp = new Block[currentForks];
		System.arraycopy(forks, 0, tmp, 0, currentForks);
		return tmp;
	}

	// TODO: do I really need to copy this?
	public int[] forksLengths() {
		final int[] tmp = new int[currentForks];
		System.arraycopy(forksDepths, 0, tmp, 0, currentForks);
		return tmp;
	}

	private void expandArrayIfNeeded() {
		if (currentForks == forks.length) {
			final int oldSize = forks.length;
			final int newSize = 3 * oldSize / 2;
			final Block[] newBlocks = new Block[newSize];
			final int[] newLengths = new int[newSize];
			System.arraycopy(forks, 0, newBlocks, 0, oldSize);
			System.arraycopy(forksDepths, 0, newLengths, 0, oldSize);
			this.forks = newBlocks;
			this.forksDepths = newLengths;
		}
	}
}
