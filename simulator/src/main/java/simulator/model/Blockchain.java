package simulator.model;

public final class Blockchain implements Cloneable {

	private static final int DEFAULT_INITIAL_CAPACITY = 50;

	// status
	private int currentForks;
	private Block[] forks;
	private int longestChainIndex;

	public Blockchain(Block genesis) {
		this.currentForks = 1;
		this.forks = new Block[DEFAULT_INITIAL_CAPACITY];
		this.forks[0] = genesis;
		this.longestChainIndex = 0;
	}

	public Block longestChain() {
		return forks[longestChainIndex];
	}

	@SuppressWarnings("WeakerAccess")
	public long forks() {
		return currentForks;
	}

	public void add(Block block) {

		// try to find the parent of this node (this is not a new fork)
		for (int i = 0; i < currentForks; i++) {
			if (forks[i] == block.previous) {

				// we add a block on top of another one...
				// ... so we remove the current one and replace it with the new block
				// this is NOT a fork!
				forks[i] = block;

				// keep track of the longest chain
				if (block.depth > longestChain().depth) {
					longestChainIndex = i;
				}

				// optimization: only one block can be the parent, exit the loop
				return;
			}
		}

		// make sure we have enough space in the array
		expandArrayIfNeeded();

		// keep track of the longest chain
		if (block.depth > longestChain().depth) {
			longestChainIndex = currentForks;
		}

		// if here, we have a new fork
		forks[currentForks] = block;
		currentForks++;
	}

	private void expandArrayIfNeeded() {
		final Block[] temp = new Block[3 * forks.length / 2];
		System.arraycopy(forks, 0, temp, 0, forks.length);
		this.forks = temp;
	}

	@Override
	public Blockchain clone() throws CloneNotSupportedException {
		final Blockchain clone = (Blockchain) super.clone();
		clone.forks = new Block[forks.length];
		System.arraycopy(forks, 0, clone.forks, 0, forks.length);
		return clone;
	}
}
