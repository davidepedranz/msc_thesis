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

import peersim.config.Configuration;

/**
 * Utilities to work with the configuration of PeerSim.
 */
public final class ConfigurationHelper {

    /**
     * Default name separator in PeerSim.
     */
    private static final String SEPARATOR = ".";

    // prevent class construction
    private ConfigurationHelper() {
    }

    /**
     * Read the protocol identifier for a protocol with the given prefix.
     *
     * @param prefix Prefix of the parameter.
     * @return Protocol identifier.
     */
    public static int readPid(String prefix) {
        return Configuration.getPid(prefix + SEPARATOR + "protocol");
    }

    /**
     * Read the configuration value with the given prefix and name as an integer
     * or return the default value if the key is not present.
     *
     * @param prefix       Prefix of the parameter.
     * @param parameter    Parameter to read.
     * @param defaultValue Default value for the parameter.
     * @return Value of the parameter in the configuration or default value, if not set.
     */
    public static int readInt(String prefix, String parameter, int defaultValue) {
        return Configuration.getInt(prefix + SEPARATOR + parameter, defaultValue);
    }

    /**
     * Read the configuration value with the given prefix and name as a boolean
     * or return the default value if the key is not present.
     *
     * @param prefix       Prefix of the parameter.
     * @param parameter    Parameter to read.
     * @param defaultValue Default value for the parameter.
     * @return Value of the parameter in the configuration or default value, if not set.
     */
    public static boolean readBoolean(String prefix, String parameter, boolean defaultValue) {
        return Configuration.getBoolean(prefix + SEPARATOR + parameter, defaultValue);
    }
}
