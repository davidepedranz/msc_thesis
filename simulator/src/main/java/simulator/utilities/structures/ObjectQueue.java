package simulator.utilities.structures;

import java.util.Arrays;
import java.util.Random;

@SuppressWarnings("unchecked")
public final class ObjectQueue<T> {

	private static final int DEFAULT_INITIAL_DIMENSION = 10;

	private final int capacity;
	private int length;
	private T[] array;

	public ObjectQueue() {
		this(DEFAULT_INITIAL_DIMENSION);
	}

	@SuppressWarnings("WeakerAccess")
	public ObjectQueue(int capacity) {
		assert capacity >= 1;
		this.capacity = capacity;
		this.length = 0;
		this.array = (T[]) new Object[capacity];
	}

	public ObjectQueue(ObjectQueue<T> original) {
		this.capacity = original.capacity;
		this.length = original.length;
		this.array = (T[]) new Object[original.array.length];
		System.arraycopy(original.array, 0, this.array, 0, original.array.length);
	}

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

	public int size() {
		return length;
	}

	public T random(Random random) {
		assert this.length > 0;
		final int index = random.nextInt(this.length);
		return this.array[index];
	}

	public T[] dump(T[] a) {
		return (T[]) Arrays.copyOf(array, length, a.getClass());
	}

	public void clear() {
		this.length = 0;
		this.array = (T[]) new Object[this.capacity];
	}

	int _arraySize() {
		return this.array.length;
	}
}
