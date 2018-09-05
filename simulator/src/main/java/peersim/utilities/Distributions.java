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

package peersim.utilities;

import peersim.core.CommonState;

/**
 * Extract random variables according to common distributions.
 */
public final class Distributions {

    // prevent class construction
    private Distributions() {
    }

    /**
     * Generate a new random variable from an exponential distribution with the given mean.
     *
     * @param mean Mean of the exponential distribution.
     * @return Value of the random variable.
     */
    static double nextExponential(double mean) {
        final double lambda = 1 / mean;
        return Math.log(1 - CommonState.r.nextDouble()) / (-lambda);
    }

    /**
     * Generate a new random variable from an exponential distribution with the given mean.
     * The value is rounded to the closest integer (or to 1 if lower than 1).
     *
     * @param mean Mean of the exponential distribution.
     * @return Value of the random variable rounded to the closest integer.
     */
    public static long nextExponentialRounded(double mean) {
        final double value = nextExponential(mean);
        final long round = Math.round(value);
        if (round == 0) {
            return round + 1;
        }
        return round;
    }
}
