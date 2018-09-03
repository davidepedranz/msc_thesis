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
 * Simple data structure that implements a queue interface using an efficient circular buffer structure.
 */
public final class CircularQueue<T> {

    private int head;
    private int tail;
    private T[] buffer;

    /**
     * Create a new circular queue with initial capacity of 1.
     */
    public CircularQueue() {
        this(1);
    }

    /**
     * Create a new circular queue with a custom initial capacity.
     *
     * @param initialCapacity Custom initial capacity.
     */
    @SuppressWarnings("unchecked")
    CircularQueue(int initialCapacity) {
        assert initialCapacity >= 1;
        this.head = 0;
        this.tail = 0;
        this.buffer = (T[]) new Object[initialCapacity];
    }

    /**
     * Create a copy of the given circular queue.
     *
     * @param original Circular queue to copy.
     */
    @SuppressWarnings("unchecked")
    public CircularQueue(CircularQueue<T> original) {
        this.head = original.head;
        this.tail = original.tail;
        this.buffer = (T[]) new Object[original.buffer.length];
        System.arraycopy(this.buffer, 0, original.buffer, 0, original.buffer.length);
    }

    /**
     * Append one element to the queue.
     *
     * @param element New element to add.
     */
    @SuppressWarnings("unchecked")
    public void enqueue(T element) {
        if (this.full()) {

            final int oldSize = this.buffer.length;
            final int newSize = 2 * oldSize;

            final T[] newBuffer = (T[]) new Object[newSize];
            final int offset = this.buffer.length - this.head;
            System.arraycopy(this.buffer, this.head, newBuffer, 0, offset);
            System.arraycopy(this.buffer, 0, newBuffer, offset, this.head);

            newBuffer[buffer.length] = element;
            this.head = 0;
            this.tail = (buffer.length + 1) % newSize;
            this.buffer = newBuffer;

        } else {
            this.buffer[this.tail] = element;
            this.tail = next(this.tail);
        }
    }

    /**
     * Return the oldest element in the queue.
     *
     * @return Oldest element.
     */
    public T head() {
        if (this.empty()) {
            return null;
        } else {
            return this.buffer[this.head];
        }
    }

    /**
     * Remove and return the oldest element in the queue.
     *
     * @return Oldest element.
     */
    public T dequeue() {
        if (this.empty()) {
            return null;
        } else {
            final T element = this.buffer[this.head];
            this.buffer[this.head] = null;
            this.head = next(this.head);
            return element;
        }
    }

    /**
     * Check if the queue is empty.
     *
     * @return True if the queue is empty, false otherwise.
     */
    public boolean empty() {
        return this.head == this.tail && this.buffer[this.head] == null;
    }

    /**
     * Check if the queue is full (and we need to resize it before storing a new element.
     *
     * @return True if the queue is full, false otherwise.
     */
    private boolean full() {
        return this.head == this.tail && this.buffer[this.head] != null;
    }

    /**
     * Add one position to the given index in the circular queue.
     * This method takes care of starting from the beginning of the queue if needed.
     *
     * @param index Original index.
     * @return Index of the following element in the queue.
     */
    private int next(int index) {
        return (index + 1) % this.buffer.length;
    }

    // test utility ONLY!
    int _bufferSize() {
        return this.buffer.length;
    }
}
