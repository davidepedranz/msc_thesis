package simulator.utilities.structures;

import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;

public final class ObjectQueueTest {

	private static final Object ELEMENT_0 = new Object();
	private static final Object ELEMENT_1 = new Object();
	private static final Object ELEMENT_2 = new Object();
	private static final Object ELEMENT_3 = new Object();
	private static final Object ELEMENT_4 = new Object();

	private final ObjectQueue<Object> queue;

	public ObjectQueueTest() {
		this.queue = new ObjectQueue<>(1);
	}

	@Test
	public void empty() {
		assertEquals(0, queue.size());
		assertEquals(1, queue._arraySize());
	}

	@Test
	public void addOne() {
		assertTrue(queue.add(ELEMENT_0));
		assertEquals(1, queue.size());

		assertFalse(queue.add(ELEMENT_0));
		assertEquals(1, queue.size());

		assertEquals(1, queue._arraySize());
		assertSame(ELEMENT_0, queue.random(new Random()));
	}

	@Test
	public void addTwo() {
		assertTrue(queue.add(ELEMENT_0));
		assertTrue(queue.add(ELEMENT_1));
		assertEquals(2, queue.size());

		assertFalse(queue.add(ELEMENT_0));
		assertFalse(queue.add(ELEMENT_1));
		assertEquals(2, queue.size());

		assertEquals(2, queue._arraySize());
	}

	@Test
	public void addThree() {
		assertTrue(queue.add(ELEMENT_0));
		assertTrue(queue.add(ELEMENT_1));
		assertTrue(queue.add(ELEMENT_2));
		assertEquals(3, queue.size());

		assertFalse(queue.add(ELEMENT_0));
		assertFalse(queue.add(ELEMENT_1));
		assertFalse(queue.add(ELEMENT_2));
		assertEquals(3, queue.size());

		assertEquals(4, queue._arraySize());
	}

	@Test
	public void addFour() {
		assertTrue(queue.add(ELEMENT_0));
		assertTrue(queue.add(ELEMENT_1));
		assertTrue(queue.add(ELEMENT_2));
		assertTrue(queue.add(ELEMENT_3));
		assertEquals(4, queue.size());

		assertFalse(queue.add(ELEMENT_0));
		assertFalse(queue.add(ELEMENT_1));
		assertFalse(queue.add(ELEMENT_2));
		assertFalse(queue.add(ELEMENT_3));
		assertEquals(4, queue.size());

		assertEquals(4, queue._arraySize());
	}

	@Test
	public void addFive() {
		assertTrue(queue.add(ELEMENT_0));
		assertTrue(queue.add(ELEMENT_1));
		assertTrue(queue.add(ELEMENT_2));
		assertTrue(queue.add(ELEMENT_3));
		assertTrue(queue.add(ELEMENT_4));
		assertEquals(5, queue.size());

		assertFalse(queue.add(ELEMENT_0));
		assertFalse(queue.add(ELEMENT_1));
		assertFalse(queue.add(ELEMENT_2));
		assertFalse(queue.add(ELEMENT_3));
		assertFalse(queue.add(ELEMENT_4));
		assertEquals(5, queue.size());

		assertEquals(8, queue._arraySize());
	}

	@Test
	public void dump() {
		addFive();

		final Object[] dump = queue.dump(new Object[0]);
		assertEquals(5, dump.length);
		assertSame(ELEMENT_0, dump[0]);
		assertSame(ELEMENT_1, dump[1]);
		assertSame(ELEMENT_2, dump[2]);
		assertSame(ELEMENT_3, dump[3]);
		assertSame(ELEMENT_4, dump[4]);
	}

	@Test
	public void clear() {
		addThree();

		queue.clear();
		assertEquals(0, queue.size());
		assertEquals(1, queue._arraySize());

		addThree();
	}

	@Test
	public void copy() {
		addThree();

		final ObjectQueue<Object> copy = new ObjectQueue<>(queue);
		assertEquals(3, copy.size());
		assertEquals(4, copy._arraySize());

		assertFalse(copy.add(ELEMENT_0));
		assertFalse(copy.add(ELEMENT_1));
		assertFalse(copy.add(ELEMENT_2));
		assertTrue(copy.add(ELEMENT_3));
		assertTrue(copy.add(ELEMENT_4));
		assertFalse(copy.add(ELEMENT_3));
		assertFalse(copy.add(ELEMENT_4));
		assertEquals(5, copy.size());
		assertEquals(8, copy._arraySize());
	}
}
