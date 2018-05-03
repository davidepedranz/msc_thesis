package simulator.utilities;

import peersim.core.CommonState;
import peersim.core.Node;

import java.util.Random;

/**
 * Utilities to shuffle an array of nodes.
 */
@SuppressWarnings("WeakerAccess")
public final class Shuffler {

	/**
	 * Implement the Fisher-Yates shuffling algorithm.
	 * NB: the array will be shuffled in place!
	 * https://en.wikipedia.org/wiki/Fisher%E2%80%93Yates_shuffle
	 *
	 * @param array Array to shuffle.
	 */
	public static void shuffleInPlace(Node[] array) {
		final Random rnd = CommonState.r;
		for (int i = array.length - 1; i > 0; i--) {
			final int index = rnd.nextInt(i + 1);
			final Node element = array[index];
			array[index] = array[i];
			array[i] = element;
		}
	}

	/**
	 * Implement the Fisher-Yates shuffling algorithm.
	 * https://en.wikipedia.org/wiki/Fisher%E2%80%93Yates_shuffle
	 *
	 * @param array Array to shuffle.
	 * @return Shuffled copy of the array.
	 */
	public static Node[] shuffle(Node[] array) {
		final Node[] shuffled = new Node[array.length];
		System.arraycopy(array, 0, shuffled, 0, array.length);
		shuffleInPlace(shuffled);
		return shuffled;
	}
}
