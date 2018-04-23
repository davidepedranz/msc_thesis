package simulator.model;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.TreeSet;

public final class Blockchain {

	private final TreeSet<Block> forks;

	public Blockchain(Block genesis) {
		this.forks = new TreeSet<>();
		this.forks.add(genesis);
	}

	@NotNull
	public Block longestChain() {
		return forks.first();
	}

	public long forks() {
		return forks.size();
	}

	public void add(Block block) {
		final Iterator<Block> iterator = forks.iterator();
		while (iterator.hasNext()) {
			final Block current = iterator.next();
			if (current == block.previous()) {

				// we add a block on top of another one...
				// ... so we remove the current one and replace it with the new block
				// this is NOT a fork!
				iterator.remove();
				forks.add(block);

				// optimization: only one block can be the parent, exit the loop
				return;
			}
		}
		// if here, we add a block which is a fork
		forks.add(block);
	}
}
