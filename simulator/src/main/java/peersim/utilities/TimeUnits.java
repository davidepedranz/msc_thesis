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

/**
 * Utilities to work with time, useful for configuration default values.
 * All values are expressed in milliseconds.
 */
public final class TimeUnits {

    /**
     * Milliseconds in 1 second.
     */
    public static final int SECONDS = 1000;

    /**
     * Milliseconds in 1 minute.
     */
    public static final int MINUTES = 60 * SECONDS;

    /**
     * Milliseconds in 1 hour.
     */
    public static final int HOURS = 60 * MINUTES;

    // prevent class construction
    private TimeUnits() {
    }
}
