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

package simulator.bitcoin.initializers;

import peersim.core.Control;

import static peersim.utilities.ConfigurationHelper.readPid;
import static peersim.utilities.SimulationUtilities.scheduleEventForAllNodes;

/**
 * {@link Control} used to initialize a protocol, using a {@link StartEvent}.
 */
public final class ProtocolInitializer implements Control {

    // fields
    private final int pid;

    /**
     * Default constructor, following the PeerSim conventions.
     * The name field is automatically provided by the PeerSim engine.
     *
     * @param prefix Name of this control in the configuration file.
     */
    public ProtocolInitializer(String prefix) {
        this.pid = readPid(prefix);
    }

    @Override
    public boolean execute() {

        // schedule the start event immediately == time zero
        final StartEvent startEvent = StartEvent.INSTANCE;
        scheduleEventForAllNodes(0, startEvent, pid);

        // false == do NOT stop the simulation
        return false;
    }
}
