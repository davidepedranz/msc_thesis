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

import static org.junit.Assert.*;

public final class CircularQueueTest {

	private static final Object ELEMENT_0 = new Object();
	private static final Object ELEMENT_1 = new Object();
	private static final Object ELEMENT_2 = new Object();
	private static final Object ELEMENT_3 = new Object();
	private static final Object ELEMENT_4 = new Object();
	private static final Object ELEMENT_5 = new Object();

	private final CircularQueue<Object> queue;

	public CircularQueueTest() {
		this.queue = new CircularQueue<>(1);
	}

	@Test
	public void empty() {
		assertNull(queue.dequeue());
	}

	@Test
	public void oneElement() {
		queue.enqueue(ELEMENT_0);
		assertSame(ELEMENT_0, queue.dequeue());
		assertNull(queue.dequeue());
		assertEquals(1, queue._bufferSize());
	}

	@Test
	public void twoElementsOrdered() {
		queue.enqueue(ELEMENT_0);
		queue.enqueue(ELEMENT_1);
		assertSame(ELEMENT_0, queue.dequeue());
		assertSame(ELEMENT_1, queue.dequeue());

		assertNull(queue.dequeue());
		assertEquals(2, queue._bufferSize());
	}

	@Test
	public void twoElementsDisordered() {
		queue.enqueue(ELEMENT_0);
		assertSame(ELEMENT_0, queue.dequeue());

		queue.enqueue(ELEMENT_1);
		queue.enqueue(ELEMENT_2);
		assertSame(ELEMENT_1, queue.dequeue());
		assertSame(ELEMENT_2, queue.dequeue());

		assertNull(queue.dequeue());
		assertEquals(2, queue._bufferSize());
	}

	@Test
	public void threeElementsOrdered() {
		queue.enqueue(ELEMENT_0);
		queue.enqueue(ELEMENT_1);
		queue.enqueue(ELEMENT_2);
		assertSame(ELEMENT_0, queue.dequeue());
		assertSame(ELEMENT_1, queue.dequeue());
		assertSame(ELEMENT_2, queue.dequeue());

		assertNull(queue.dequeue());
		assertEquals(4, queue._bufferSize());
	}

	@Test
	public void threeElementsDisordered() {
		queue.enqueue(ELEMENT_0);
		assertSame(ELEMENT_0, queue.dequeue());

		queue.enqueue(ELEMENT_1);
		queue.enqueue(ELEMENT_2);
		queue.enqueue(ELEMENT_3);
		assertSame(ELEMENT_1, queue.dequeue());
		assertSame(ELEMENT_2, queue.dequeue());
		assertSame(ELEMENT_3, queue.dequeue());

		assertNull(queue.dequeue());
		assertEquals(4, queue._bufferSize());
	}

	@Test
	public void fourElementsOrdered() {
		queue.enqueue(ELEMENT_0);
		queue.enqueue(ELEMENT_1);
		queue.enqueue(ELEMENT_2);
		queue.enqueue(ELEMENT_3);
		assertSame(ELEMENT_0, queue.dequeue());
		assertSame(ELEMENT_1, queue.dequeue());
		assertSame(ELEMENT_2, queue.dequeue());
		assertSame(ELEMENT_3, queue.dequeue());

		assertNull(queue.dequeue());
		assertEquals(4, queue._bufferSize());
	}

	@Test
	public void fourElementsDisordered() {
		queue.enqueue(ELEMENT_0);
		queue.enqueue(ELEMENT_1);
		assertSame(ELEMENT_0, queue.dequeue());
		assertSame(ELEMENT_1, queue.dequeue());

		queue.enqueue(ELEMENT_2);
		queue.enqueue(ELEMENT_3);
		queue.enqueue(ELEMENT_4);
		queue.enqueue(ELEMENT_5);
		assertSame(ELEMENT_2, queue.dequeue());
		assertSame(ELEMENT_3, queue.dequeue());
		assertSame(ELEMENT_4, queue.dequeue());
		assertSame(ELEMENT_5, queue.dequeue());

		assertNull(queue.dequeue());
		assertEquals(4, queue._bufferSize());
	}
}
