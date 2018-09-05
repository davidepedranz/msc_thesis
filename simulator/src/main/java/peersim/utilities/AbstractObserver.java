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
import peersim.core.Control;
import peersim.core.Network;
import peersim.util.IncrementalStats;

import static peersim.utilities.ConfigurationHelper.readPid;

/**
 * Base class that allows to monitor a single parameter of a protocol.
 */
public abstract class AbstractObserver implements Control {

    // fields
    private final String prefix;
    private final int pid;

    /**
     * Default constructor, following the PeerSim conventions.
     * The prefix field is automatically provided by the PeerSim engine.
     *
     * @param prefix Prefix of this control in the configuration file.
     */
    protected AbstractObserver(String prefix) {
        this.prefix = prefix;
        this.pid = readPid(prefix);
    }

    @Override
    public boolean execute() {

        // compute the statistics over all nodes in the network
        final IncrementalStats stats = new IncrementalStats();
        for (int i = 0; i < Network.size(); i++) {
            final double value = getValue(i, pid);
            stats.add(value);
        }

        // print them out, following PeerSim conventions
        System.out.println(prefix + ": [" + CommonState.getTime() + "] " + stats);

        // false == do NOT stop the simulation
        return false;
    }

    /**
     * Get the value to observe from a protocol.
     *
     * @param index Index of the node from which to getBlock the value.
     * @param pid   ID of the protocol.
     * @return The value to observe.
     */
    protected abstract double getValue(int index, int pid);
}
