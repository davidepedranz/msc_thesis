package simulator.utilities;

import peersim.core.CommonState;

import java.util.Random;

/**
 * Extract random variables according to some distribution.
 */
public final class Distributions {

	private static final Random random = CommonState.r;

	// used only for the tests!
	static double nextExponential(double mean) {
		final double lambda = 1 / mean;
		return Math.log(1 - random.nextDouble()) / (-lambda);
	}

	// NB: we use floor to avoid returning zero!
	public static long roundedNextExponential(double mean) {
		final long round = Math.round(nextExponential(mean));
		if (round == 0) {
			return round + 1;
		}
		return round;
	}
}
