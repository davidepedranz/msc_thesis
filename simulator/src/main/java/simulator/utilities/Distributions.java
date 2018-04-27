package simulator.utilities;

import peersim.core.CommonState;

import java.util.Random;

/**
 * Extract random variables according to some distribution.
 */
public final class Distributions {

	private static final Random random = CommonState.r;

	public static double nextExponential(double mean) {
		final double lambda = 1 / mean;
		return Math.log(1 - random.nextDouble()) / (-lambda);
	}

	public static long roundedNextExponential(double mean) {
		return Math.round(nextExponential(mean));
	}
}
