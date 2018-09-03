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

/**
 * Simple data structure that stores a list of integer in an array.
 */
public final class IntList {

    private int[] array;
    private int length;

    /**
     * Default constructor.
     */
    public IntList() {
        this.array = new int[2];
        this.length = 0;
    }

    /**
     * @return True if the list does not contain any element, false otherwise.
     */
    public boolean isEmpty() {
        return this.length == 0;
    }

    /**
     * @return The number of elements stored in the list.
     */
    public int size() {
        return this.length;
    }

    /**
     * Lookup an element by index.
     *
     * @param index Index of the element.
     * @return Element stored in the given position.
     */
    public int get(int index) {
        return this.array[index];
    }

    /**
     * Add an element to the list (after the last one).
     *
     * @param element Element to add.
     */
    public void add(int element) {
        if (this.array.length == this.length) {
            final int oldSize = this.length;
            final int newSize = 3 * oldSize / 2;
            final int[] newArray = new int[newSize];
            System.arraycopy(this.array, 0, newArray, 0, oldSize);
            this.array = newArray;
        }
        this.array[this.length] = element;
        this.length++;
    }
}
