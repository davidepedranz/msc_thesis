/*
 * Copyright (c) 2018 Davide Pedranz. All rights reserved.
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package simulator.collections;

import org.junit.Test;
import simulator.bitcoin.core.model.Block;
import simulator.bitcoin.core.model.Blocks;
import simulator.bitcoin.core.model.TransactionsWrapper;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;

import static org.junit.Assert.assertEquals;
import static simulator.collections.ArrayUtilities.binarySearch;

public final class ArrayUtilitiesTest {

    private final BiFunction<Block, Long, Boolean> comparator = (block, timestamp) -> block.timestamp <= timestamp;

    private final Block a = Blocks.GENESIS;
    private final Block b = Blocks.nextBlock(a, new TransactionsWrapper(0), 5);
    private final Block c = Blocks.nextBlock(b, new TransactionsWrapper(0), 10);
    private final List<Block> blocks = Arrays.asList(a, b, c);

    @Test
    public void testBinarySearchWithNegativeTimestamp() {
        int index = binarySearch(blocks, -1, comparator);
        assertEquals(-1, index);
    }

    @Test
    public void testBinarySearchWithZeroItems() {
        final List<Block> blocks = Collections.emptyList();
        assertEquals(-1, binarySearch(blocks, Long.MAX_VALUE, comparator));
    }

    @Test
    public void testBinarySearchWithOneItem() {
        final List<Block> blocks = Collections.singletonList(Blocks.GENESIS);
        assertEquals(0, binarySearch(blocks, 0, comparator));
    }

    @Test
    public void testBinarySearchWithManyItems() {
        assertEquals(0, binarySearch(blocks, 3, comparator));
        assertEquals(1, binarySearch(blocks, 5, comparator));
        assertEquals(2, binarySearch(blocks, 19, comparator));
    }
}
