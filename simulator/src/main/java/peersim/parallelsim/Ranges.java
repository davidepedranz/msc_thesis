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

package peersim.parallelsim;

import peersim.config.Configuration;
import peersim.config.IllegalParameterException;
import peersim.util.StringListParser;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Utility class that allows to load a PeerSim configuration file
 * and compute all combinations of the parameter values (from their ranges).
 */
final class Ranges {

    /**
     * Names of range parameters.
     */
    private final String[] parameters;

    /**
     * Values to be simulated, for each parameter.
     */
    private final String[][] values;

    /**
     * Constructor.
     *
     * @param rangeNames Name of the ranges in the properties file.
     */
    Ranges(String[] rangeNames) {

        // allocate space for the parameters and its values
        this.parameters = new String[rangeNames.length];
        this.values = new String[rangeNames.length][];

        // load the parameters
        for (int i = 0; i < this.parameters.length; i++) {
            final String rawRange = Configuration.getString(rangeNames[i]);
            final String[] tokens = rawRange.split(";");
            if (tokens.length != 2) {
                throw new IllegalParameterException(rangeNames[i], " should be formatted as <parameter>;<value list>");
            }
            this.parameters[i] = tokens[0];
            this.values[i] = StringListParser.parseList(tokens[1]);
        }
    }

    /**
     * @return Instance of {@link Iterator} that iterates over
     * all possible combinations of the ranges parameters.
     */
    Iterator<String[]> iterator() {
        final int[] indexes = new int[values.length];
        return new Iterator<String[]>() {

            @Override
            public boolean hasNext() {
                return indexes[0] < values[0].length;
            }

            @Override
            public String[] next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                } else {
                    final String[] current = currentCombination();
                    incrementIndexes();
                    return current;
                }
            }

            private String[] currentCombination() {
                final String[] current = new String[parameters.length];
                for (int i = 0; i < current.length; i++) {
                    current[i] = parameters[i] + "=" + values[i][indexes[i]];
                }
                return current;
            }

            private void incrementIndexes() {
                indexes[indexes.length - 1]++;
                for (int i = indexes.length - 1; i > 0; i--) {
                    if (indexes[i] == values[i].length) {
                        indexes[i] = 0;
                        indexes[i - 1]++;
                    }
                }
            }
        };
    }
}
