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

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;
import peersim.util.IncrementalFreq;
import simulator.bitcoin.topology.BitcoinTopology;

/**
 * Observe the topology created by Bitcoin nodes in the {@link BitcoinTopology} protocol.
 */
public final class TopologyObserver implements Control {

    // parameters
    private static final String PARAMETER_PROTOCOL = "protocol";

    // fields
    private final String prefix;
    private final int pid;

    /**
     * Default constructor, following the PeerSim conventions.
     * The prefix field is automatically provided by the PeerSim engine.
     *
     * @param prefix Prefix of this control in the configuration file.
     */
    public TopologyObserver(String prefix) {
        this.prefix = prefix;
        this.pid = Configuration.getPid(prefix + "." + PARAMETER_PROTOCOL);
    }

    @Override
    public boolean execute() {

        // compute the statistics over all nodes in the network
        final IncrementalFreq outgoing = new IncrementalFreq();
        final IncrementalFreq incoming = new IncrementalFreq();
        final IncrementalFreq peers = new IncrementalFreq();
        for (int i = 0; i < Network.size(); i++) {
            final BitcoinTopology protocol = (BitcoinTopology) Network.get(i).getProtocol(pid);
            outgoing.add(protocol.degreeOutgoing());
            incoming.add(protocol.degreeIncoming());
            peers.add(protocol.peers());
        }

        // print them out, following PeerSim conventions
        System.out.println(prefix + "-out: [" + CommonState.getTime() + "] " + outgoing);
        System.out.println(prefix + "-in: [" + CommonState.getTime() + "] " + incoming);
        System.out.println(prefix + "-peers: [" + CommonState.getTime() + "] " + peers);

        // false == do NOT stop the simulation
        return false;
    }
}
