package simulator.utilities.structures;

import org.junit.Test;
import simulator.bitcoin.core.model.Block;
import simulator.bitcoin.core.model.Blocks;
import simulator.bitcoin.core.model.TransactionsWrapper;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;

import static org.junit.Assert.assertEquals;
import static simulator.utilities.structures.ArrayUtilities.binarySearch;

public final class ArrayUtilitiesTest {

	private final BiFunction<Block, Long, Boolean> comparator = (block, timestamp) -> block.timestamp <= timestamp;

	private final Block a = Blocks.GENESIS;
	private final Block b = Blocks.nextBlock(a, new TransactionsWrapper(0), 2, 5);
	private final Block c = Blocks.nextBlock(b, new TransactionsWrapper(0), 3, 10);
	private final List<Block> blocks = Arrays.asList(a, b, c);

	@Test
	public void binarySearchNegativeTimestamp() {
		int index = binarySearch(blocks, -1, comparator);
		assertEquals(-1, index);
	}

	@Test
	public void binarySearchZeroItems() {
		final List<Block> blocks = Collections.emptyList();
		assertEquals(-1, binarySearch(blocks, Long.MAX_VALUE, comparator));
	}

	@Test
	public void binarySearchOneItem() {
		final List<Block> blocks = Collections.singletonList(Blocks.GENESIS);
		assertEquals(0, binarySearch(blocks, 0, comparator));
	}

	@Test
	public void binarySearchManyItems() {
		assertEquals(0, binarySearch(blocks, 3, comparator));
		assertEquals(1, binarySearch(blocks, 5, comparator));
		assertEquals(2, binarySearch(blocks, 19, comparator));
	}
}
