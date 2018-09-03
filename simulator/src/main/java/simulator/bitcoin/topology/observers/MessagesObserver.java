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

import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;
import peersim.util.IncrementalStats;
import simulator.bitcoin.topology.BitcoinTopology;

import static peersim.utilities.ConfigurationHelper.readPid;

/**
 * Observer of the number of control messages used by the {@link BitcoinTopology} protocol.
 */
public final class MessagesObserver implements Control {

    // fields
    private final String prefix;
    private final int pid;

    /**
     * Default constructor, following the PeerSim conventions.
     * The prefix field is automatically provided by the PeerSim engine.
     *
     * @param prefix Prefix of this control in the configuration file.
     */
    public MessagesObserver(String prefix) {
        this.prefix = prefix;
        this.pid = readPid(prefix);
    }

    @Override
    public boolean execute() {

        // compute the statistics over all nodes in the network
        final IncrementalStats versionMessages = new IncrementalStats();
        final IncrementalStats verAckMessages = new IncrementalStats();
        final IncrementalStats getAddrMessages = new IncrementalStats();
        final IncrementalStats addrMessages = new IncrementalStats();
        final IncrementalStats pingMessages = new IncrementalStats();
        final IncrementalStats pongMessages = new IncrementalStats();
        for (int i = 0; i < Network.size(); i++) {
            final BitcoinTopology protocol = (BitcoinTopology) Network.get(i).getProtocol(pid);
            versionMessages.add(protocol.versionMessages());
            verAckMessages.add(protocol.verAckMessages());
            getAddrMessages.add(protocol.getAddrMessages());
            addrMessages.add(protocol.addrMessages());
            pingMessages.add(protocol.pingMessages());
            pongMessages.add(protocol.pongMessages());
        }

        // print them out, following PeerSim conventions
        System.out.println(prefix + "-version: [" + CommonState.getTime() + "] " + versionMessages);
        System.out.println(prefix + "-verAck: [" + CommonState.getTime() + "] " + verAckMessages);
        System.out.println(prefix + "-getAddr: [" + CommonState.getTime() + "] " + getAddrMessages);
        System.out.println(prefix + "-addr: [" + CommonState.getTime() + "] " + addrMessages);
        System.out.println(prefix + "-ping: [" + CommonState.getTime() + "] " + pingMessages);
        System.out.println(prefix + "-pong: [" + CommonState.getTime() + "] " + pongMessages);

        // false == do NOT stop the simulation
        return false;
    }
}
