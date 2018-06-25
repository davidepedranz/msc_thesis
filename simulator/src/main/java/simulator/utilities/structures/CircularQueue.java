package simulator.utilities.structures;

/**
 * Simple data structure that implements a queue interface
 * using an efficient circular buffer structure.
 */
public final class CircularQueue<T> {

	private int head;
	private int tail;
	private T[] buffer;

	public CircularQueue() {
		this(1);
	}

	@SuppressWarnings({"unchecked", "WeakerAccess"})
	public CircularQueue(int initialCapacity) {
		assert initialCapacity >= 1;
		this.head = 0;
		this.tail = 0;
		this.buffer = (T[]) new Object[initialCapacity];
	}

	@SuppressWarnings("unchecked")
	public CircularQueue(CircularQueue<T> original) {
		this.head = original.head;
		this.tail = original.tail;
		this.buffer = (T[]) new Object[original.buffer.length];
		System.arraycopy(this.buffer, 0, original.buffer, 0, original.buffer.length);
	}

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

	public T head() {
		if (this.empty()) {
			return null;
		} else {
			return this.buffer[this.head];
		}
	}

	public boolean empty() {
		return this.head == this.tail && this.buffer[this.head] == null;
	}

	private boolean full() {
		return this.head == this.tail && this.buffer[this.head] != null;
	}

	private int next(int index) {
		return (index + 1) % this.buffer.length;
	}

	int _bufferSize() {
		return this.buffer.length;
	}
}
