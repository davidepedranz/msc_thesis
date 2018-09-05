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
import peersim.core.Network;
import peersim.core.Node;
import peersim.edsim.EDSimulator;

/**
 * Class of utilities to interact with the {@link peersim.core.Network}
 * and {@link peersim.edsim.EDSimulator} objects of PeerSim.
 */
public final class SimulationUtilities {

    // prevent class construction
    private SimulationUtilities() {
    }

    /**
     * Schedules the given event to one random node that implements the given protocol.
     *
     * @param delay Delay (time units from now) at which to schedule the event.
     * @param event Event to schedule to ALL nodes implementing the protocol.
     * @param pid   ID of the protocol that will receive the given event.
     */
    public static void scheduleEventForRandomNode(long delay, Object event, int pid) {
        final int nodeIndex = CommonState.r.nextInt(Network.size());
        EDSimulator.add(delay, event, Network.get(nodeIndex), pid);
    }

    /**
     * Schedules the given event to ALL nodes that implements the given protocol.
     *
     * @param delay Delay (time units from now) at which to schedule the event.
     * @param event Event to schedule to ALL nodes implementing the protocol.
     * @param pid   ID of the protocol that will receive the given event.
     */
    public static void scheduleEventForAllNodes(long delay, Object event, int pid) {
        for (int i = 0; i < Network.size(); i++) {
            final Node node = Network.get(i);
            EDSimulator.add(delay, event, node, pid);
        }
    }
}
