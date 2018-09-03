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

import java.util.Arrays;
import java.util.Random;

/**
 * Simple data structure that stores a list of objects in an array
 * and provides convenient utilities to access them.
 */
public final class ObjectList<T> {

    /**
     * Default initial capacity of the underlying array.
     */
    private static final int DEFAULT_INITIAL_CAPACITY = 10;

    private final int capacity;
    private int length;
    private T[] array;

    /**
     * Create a new list with the default initial capacity.
     */
    public ObjectList() {
        this(DEFAULT_INITIAL_CAPACITY);
    }

    /**
     * Create a new list with a custom initial capacity.
     *
     * @param initialCapacity Custom initial capacity.
     */
    @SuppressWarnings("unchecked")
    ObjectList(int initialCapacity) {
        assert initialCapacity >= 1;
        this.capacity = initialCapacity;
        this.length = 0;
        this.array = (T[]) new Object[initialCapacity];
    }

    /**
     * Create a copy of the given list.
     *
     * @param original List to copy.
     */
    @SuppressWarnings("unchecked")
    public ObjectList(ObjectList<T> original) {
        this.capacity = original.capacity;
        this.length = original.length;
        this.array = (T[]) new Object[original.array.length];
        System.arraycopy(original.array, 0, this.array, 0, original.array.length);
    }

    /**
     * Append one element to the list.
     *
     * @param element New element to add.
     */
    @SuppressWarnings("unchecked")
    public boolean add(T element) {

        // do not allow duplicates
        for (int i = 0; i < length; i++) {
            if (array[i] == element) {
                return false;
            }
        }

        // dynamically resize the array if needed
        if (this.array.length == this.length) {
            final int oldSize = this.length;
            final int newSize = 2 * oldSize;
            final T[] newArray = (T[]) new Object[newSize];
            System.arraycopy(this.array, 0, newArray, 0, oldSize);
            this.array = newArray;
        }

        // addBlock the element to the last position
        this.array[this.length] = element;
        this.length++;

        // the element was added
        return true;
    }

    /**
     * @return The number of elements stored in the list.
     */
    public int size() {
        return length;
    }

    /**
     * Return a random element stored in the list.
     *
     * @param random Random number generator used to choose the random element.
     * @return Random element stored in the list.
     */
    public T random(Random random) {
        assert this.length > 0;
        final int index = random.nextInt(this.length);
        return this.array[index];
    }

    /**
     * Dump the elements stored in the list as an array.
     *
     * @param emptyArray Array of type T.
     * @return Copy of the list as an array.
     */
    @SuppressWarnings("unchecked")
    public T[] dump(T[] emptyArray) {
        return (T[]) Arrays.copyOf(this.array, length, emptyArray.getClass());
    }

    /**
     * Remove all elements from the list.
     */
    @SuppressWarnings("unchecked")
    public void clear() {
        this.length = 0;
        this.array = (T[]) new Object[this.capacity];
    }

    // test utility ONLY!
    int _arraySize() {
        return this.array.length;
    }
}
