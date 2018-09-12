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

package simulator.attacks;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Node;
import peersim.edsim.EDSimulator;
import peersim.transport.Transport;
import simulator.bitcoin.core.messages.BlockMessage;

import static peersim.utilities.ConfigurationHelper.readInt;

/**
 * {@link Transport} used to simulate a Balance attack:
 * it will create 2 partitions of nodes and delay / drop messages between different partitions.
 */
public final class BalanceAttackTransport implements Transport {

    // configuration parameters
    private static final String PARAM_UNDERLING_TRANSPORT = "transport";
    private static final String PARAM_DELAY = "delay";
    private static final String PARAM_DROP_PROBABILITY = "drop";
    private static final String PARAM_PARTITIONS = "partitions";

    // default values
    private static final int DEFAULT_PARTITIONS = 2;

    // configuration actual values
    private final int transport;
    private final long delay;
    private final float drop;
    private final int partitions;

    /**
     * Default constructor, following the PeerSim conventions.
     * The name field is automatically provided by the PeerSim engine.
     *
     * @param name Name of this transport in the configuration file.
     */
    public BalanceAttackTransport(String name) {
        this.transport = Configuration.getPid(name + "." + PARAM_UNDERLING_TRANSPORT);
        this.delay = Configuration.getLong(name + "." + PARAM_DELAY);
        this.drop = (float) Configuration.getDouble(name + "." + PARAM_DROP_PROBABILITY);
        this.partitions = readInt(name, PARAM_PARTITIONS, DEFAULT_PARTITIONS);

        // security check: it does not make sense to use less than 2 partitions
        if (this.partitions < 2) {
            throw new IllegalArgumentException("Parameter " + PARAM_PARTITIONS + " must be at least 2!");
        }
    }

    @Override
    public void send(Node src, Node dest, Object message, int pid) {

        // get the original underling transport
        final Transport t;
        try {
            t = (Transport) src.getProtocol(transport);
        } catch (ClassCastException e) {
            final String msg = "Protocol " + Configuration.lookupPid(transport) + " does not implement Transport";
            throw new IllegalArgumentException(msg, e);
        }

        // this transport only modifies messages between nodes in different partitions
        final boolean samePartition = inSamePartition(src, dest);

        // do nothing if the nodes are in the same partition
        if (samePartition) {
            t.send(src, dest, message, pid);
        }

        // if different partitions: delay + possibly drop the block messages
        else {

            // do nothing if the message is NOT a block
            if (!(message instanceof BlockMessage)) {
                t.send(src, dest, message, pid);
            }

            // delay blocks messages
            else {

                // send the message only if it is not lost
                final float random = CommonState.r.nextFloat();
                if (random >= drop) {

                    // schedule the delivery at a later time
                    final long latency = getLatency(src, dest);
                    EDSimulator.add(latency, message, dest, pid);
                }
            }
        }
    }

    @Override
    public long getLatency(Node src, Node dest) {
        final Transport t = (Transport) src.getProtocol(this.transport);
        final long baseLatency = t.getLatency(src, dest);
        final long extraLatency = inSamePartition(src, dest) ? 0 : delay;
        return baseLatency + extraLatency;
    }

    @Override
    @SuppressWarnings("MethodDoesntCallSuperMethod")
    public Object clone() {
        return this;
    }

    /**
     * Check if the nodes are in the same partition.
     * Nodes are partitioned in groups of equal sizes, depending on their ID.
     *
     * @param a First node.
     * @param b Second node.
     * @return True if the nodes are in the same partition, false otherwise.
     */
    private boolean inSamePartition(Node a, Node b) {
        final long aPartition = partition(a);
        final long bPartition = partition(b);
        return aPartition == bPartition;
    }

    /**
     * Computes the partition number of a given node.
     *
     * @param node Node.
     * @return Partition to which the node belongs.
     */
    private long partition(Node node) {
        return node.getID() / partitions;
    }
}
