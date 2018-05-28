package simulator.model;

import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.BitSet;
import java.util.LinkedList;
import java.util.List;

public final class Blockchain implements Cloneable {

	private static final int DEFAULT_INITIAL_CAPACITY = 50;

	// status
	private int currentForks;
	private Block[] forks;
	private int longestChainIndex;
	private int[] forksLengths;      // metric: distance from each leaf from the common parent with the longest chain
	private BitSet seenBlocks;

	public Blockchain(Block genesis) {
		this.currentForks = 1;
		this.forks = new Block[DEFAULT_INITIAL_CAPACITY];
		this.forks[0] = genesis;
		this.longestChainIndex = 0;
		this.forksLengths = new int[DEFAULT_INITIAL_CAPACITY];
		this.forksLengths[0] = 0;

		this.seenBlocks = new BitSet();
		this.seenBlocks.set(genesis.id);
	}

	public Block longestChain() {
		return forks[longestChainIndex];
	}

	@SuppressWarnings("WeakerAccess")
	public long forksNumber() {
		return currentForks;
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
		System.arraycopy(forksLengths, 0, tmp, 0, currentForks);
		return tmp;
	}

	public boolean has(int blockID) {
		return seenBlocks.get(blockID);
	}

	@Nullable
	public Block get(int blockID) {
		if (seenBlocks.get(blockID)) {
			return Block.blocks[blockID];
		} else {
			return null;
		}
	}

	@SuppressWarnings("StatementWithEmptyBody")
	public boolean add(Block block) {

		if (block.id == 1278) {
			int a = 1;
		}

		// check that I actually have seen the parent
		if (!seenBlocks.get(block.previous.id)) {
			return false;
		}

		// if I have seen the parent, I also saw this block, so I can add it
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

			if (forks[i] == block.previous) {

				// we add a block on top of another one...
				// ... so we remove the current one and replace it with the new block
				// this is NOT a new fork!
				forks[i] = block;

				// maybe we need to recompute the distances from the longest chain...
				final boolean extendingLongestChain = i == longestChainIndex;
				if (extendingLongestChain) {
					// do nothing, we replaced already the longest chain
					// the distances of the forks from the common parent with the longest chain do not change
				} else {

					// this fork is now the longest chain!
					if (block.height > longestChain().height) {
						longestChainIndex = i;
						recomputeDepths();
					}

					// this fork is not the longest chain
					else {
						// we are now a step further away from the longest chain
						forksLengths[i]++;
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
		if (block.height > longestChain().height)
			assert block.height <= longestChain().height : "a new fork cannot be the longest chain";
		forksLengths[newIndex] = computeDepth(block, longestChain());

		return true;
	}

	@SuppressWarnings("WhileLoopReplaceableByForEach")
	public List<Block> descendants(Block block) {
		final LinkedList<Block> descendants = new LinkedList<>();
		final LinkedList<Block> toVisit = new LinkedList<>();

		// initialization
		toVisit.add(block);

		// visit
		Block current;
		while ((current = toVisit.pollFirst()) != null) {
			toVisit.addAll(Arrays.asList(current.children).subList(0, current.childrenNumber));
			descendants.add(current);
		}

		return descendants;
	}

	private void recomputeDepths() {
		final Block longestChain = forks[longestChainIndex];
		for (int i = 0; i < currentForks; i++) {
			forksLengths[i] = computeDepth(forks[i], longestChain);
		}
	}

	private int computeDepth(Block current, Block longestChain) {
		int steps = 0;

		// base case: we got to the same block
		if (current == longestChain) {
			return steps;
		}

		// get to the parent of the longest longest chain at the same height of the other block
		final int currentNodeDepth = current.height;
		while (longestChain.height > currentNodeDepth) {
			longestChain = longestChain.previous;
		}

		// go on step by step, till a match is found
		while (current != longestChain) {
			current = current.previous;
			longestChain = longestChain.previous;
			steps++;
		}

		return steps;
	}

	private void expandArrayIfNeeded() {
		if (currentForks == forks.length) {
			final int oldSize = forks.length;
			final int newSize = 3 * oldSize / 2;
			final Block[] newBlocks = new Block[newSize];
			final int[] newDistances = new int[newSize];
			System.arraycopy(forks, 0, newBlocks, 0, oldSize);
			System.arraycopy(forksLengths, 0, newDistances, 0, oldSize);
			this.forks = newBlocks;
			this.forksLengths = newDistances;
		}
	}

	@Override
	public Blockchain clone() throws CloneNotSupportedException {
		final Blockchain clone = (Blockchain) super.clone();
		clone.forks = new Block[forks.length];
		System.arraycopy(forks, 0, clone.forks, 0, forks.length);
		clone.seenBlocks = (BitSet) seenBlocks.clone();
		return clone;
	}
}
