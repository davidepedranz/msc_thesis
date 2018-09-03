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

import peersim.core.CommonState;
import peersim.core.Node;

import java.util.List;
import java.util.function.BiFunction;

/**
 * Utilities to work with arrays and lists.
 */
public final class ArrayUtilities {

    // prevent class construction
    private ArrayUtilities() {
    }

    /**
     * Implement the Fisher-Yates shuffling algorithm.
     * NB: the array will be shuffled in place!
     * https://en.wikipedia.org/wiki/Fisher%E2%80%93Yates_shuffle
     *
     * @param array Array to shuffle.
     */
    public static void shuffleInPlace(Node[] array) {
        for (int i = array.length - 1; i > 0; i--) {
            final int index = CommonState.r.nextInt(i + 1);
            final Node element = array[index];
            array[index] = array[i];
            array[i] = element;
        }
    }

    /**
     * Select (at most) n random integers from the interval
     */
    public static int[] selectNRandomIntegers(int n, int bound) {
        final int realN = Math.min(n, bound);
        final int[] nodes = new int[realN];
        int selected = 0;
        while (selected < realN) {
            final int nextTry = CommonState.r.nextInt(bound);
            boolean pick = true;
            for (int i = 0; i < selected; i++) {
                if (nextTry == nodes[i]) {
                    pick = false;
                    break;
                }
            }
            if (pick) {
                nodes[selected] = nextTry;
                selected++;
            }
        }
        return nodes;
    }

    /**
     * Binary search that returns the index element with the bigger timestamp less than the given one.
     * This function runs in O(log(n)) where n is the dimension of items.
     *
     * @param items      Items to search to.
     * @param timestamp  Maximum timestamp.
     * @param comparator Function that compares an item to the a timestamp and returns true if the item
     *                   hasBlock a timestamp lower or equal to the given one, false otherwise.
     * @param <T>        Some type.
     * @return Index of the element with the bigger timestamp lower than the given one. If no element has a
     * timestamp lower than the maximum allowed timestamp, this method returns -1.
     */
    public static <T> int binarySearch(List<T> items, long timestamp, BiFunction<T, Long, Boolean> comparator) {
        int result = -1;
        int start = 0;
        int end = items.size() - 1;
        while (start <= end) {
            final int middle = (start + end) / 2;
            final T current = items.get(middle);
            final boolean lessOrEqual = comparator.apply(current, timestamp);
            if (lessOrEqual) {
                result = middle;
                start = middle + 1;
            } else {
                end = middle - 1;
            }
        }
        return result;
    }
}
