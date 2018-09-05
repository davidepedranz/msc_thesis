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

import peersim.config.FastConfig;
import peersim.core.Linkable;
import peersim.core.Node;
import peersim.transport.Transport;

/**
 * Utilities to work with the networking layer of PeerSim.
 */
public final class NetworkUtilities {

    // prevent class construction
    private NetworkUtilities() {
    }

    /**
     * Send a message to the given node for the given protocol.
     *
     * @param from    Sender node.
     * @param to      Receiver node.
     * @param pid     Protocol identifier (a protocol can send messages only to other instances of the same protocol).
     * @param message Message to send.
     */
    public static void send(Node from, Node to, int pid, Object message) {
        final Transport transport = (Transport) from.getProtocol(FastConfig.getTransport(pid));
        transport.send(from, to, message, pid);
    }

    /**
     * Send a message to all neighbours of the given node for the given protocol.
     *
     * @param from    Sender node.
     * @param pid     Protocol identifier (a protocol can send messages only to other instances of the same protocol).
     * @param message Message to send. Please note that the SAME instance of the message will be sent to all nodes.
     */
    public static void broadcast(Node from, int pid, Object message) {
        final Linkable linkable = (Linkable) from.getProtocol(FastConfig.getLinkable(pid));
        final Transport transport = (Transport) from.getProtocol(FastConfig.getTransport(pid));
        for (int i = 0; i < linkable.degree(); i++) {
            if (from.getIndex() != i) {
                transport.send(from, linkable.getNeighbor(i), message, pid);
            }
        }
    }
}
