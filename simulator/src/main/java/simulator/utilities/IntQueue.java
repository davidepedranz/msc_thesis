package simulator.utilities;

/**
 * Simple data structure that stores a list of integer in an array
 * and provides a Queue interface.
 */
public final class IntQueue {

	public int[] array;
	public int length;

	public IntQueue() {
		this.array = new int[1];
		this.length = 0;
	}

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

	public boolean isEmpty() {
		return this.length == 0;
	}
}
