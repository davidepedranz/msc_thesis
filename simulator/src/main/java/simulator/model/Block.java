package simulator.model;

import com.google.auto.value.AutoValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

@AutoValue
public abstract class Block implements Comparable<Block> {

	// counter for blocks
	private static int BLOCK_ID = 0;

	private static int nextBlock() {
		return BLOCK_ID++;
	}

	public static Block GENESIS = genesis();

	private static Block genesis() {
		return new AutoValue_Block(nextBlock(), 0, null, Collections.emptyList(), -1);
	}

	public static Block create(Block previous, List<Transaction> transactions, long miner) {
		return new AutoValue_Block(nextBlock(), previous.depth() + 1, previous, transactions, miner);
	}

	public abstract int id();

	public abstract int depth();

	@Nullable
	public abstract Block previous();

	public abstract List<Transaction> transactions();

	public abstract long miner();

	@Override
	public int compareTo(@NotNull Block o) {
		return depth() - o.depth();
	}

	@Override
	public String toString() {
		return String.format("Block{id=%s, previous=%s, depth=%s}", id(), previous().id(), depth());
	}
}
