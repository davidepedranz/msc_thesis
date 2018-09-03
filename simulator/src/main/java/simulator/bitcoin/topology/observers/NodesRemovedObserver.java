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

package simulator.bitcoin.topology.observers;

import peersim.core.Network;
import peersim.utilities.AbstractObserver;
import simulator.bitcoin.topology.BitcoinTopology;

/**
 * Observer of the number of nodes removed because of pong timeouts in the {@link BitcoinTopology} protocol.
 */
public final class NodesRemovedObserver extends AbstractObserver {

    /**
     * Default constructor, following the PeerSim conventions.
     * The prefix field is automatically provided by the PeerSim engine.
     *
     * @param prefix Prefix of this control in the configuration file.
     */
    public NodesRemovedObserver(String prefix) {
        super(prefix);
    }

    @Override
    protected double getValue(int index, int pid) {
        final BitcoinTopology protocol = (BitcoinTopology) Network.get(index).getProtocol(pid);
        return protocol.nodesRemovedForPongTimeout();
    }
}
