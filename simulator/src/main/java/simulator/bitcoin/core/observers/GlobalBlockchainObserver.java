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

package simulator.bitcoin.core.observers;

import peersim.core.CommonState;
import peersim.core.Control;
import peersim.util.IncrementalFreq;
import simulator.bitcoin.core.model.Blockchain;
import simulator.bitcoin.core.model.Blocks;

/**
 * Observer that tracks the structure of the blockchain generated by the protocol.
 * Differently from {@link BlockchainObserver} that checks the blockchain status from
 * the nodes of the protocol, we keep the global status of the blockchain here, i.e.
 * we keep track of every valid block generated during the simulation by any node,
 * even if the block has not been yet propagated.
 */
public final class GlobalBlockchainObserver implements Control {

    // fields
    private final String prefix;

    /**
     * Default constructor, following the PeerSim conventions.
     * The prefix field is automatically provided by the PeerSim engine.
     *
     * @param prefix Prefix of this control in the configuration file.
     */
    public GlobalBlockchainObserver(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public boolean execute() {

        // take into account ONLY the global state of the simulator
        final IncrementalFreq stats = new IncrementalFreq();
        final Blockchain blockchain = Blocks.getGlobalBlockchain();
        final int[] values = blockchain.forksLengths();
        for (int value : values) {
            stats.add(value);
        }

        // print them out, following PeerSim conventions
        System.out.println(prefix + ": [" + CommonState.getTime() + "] " + stats);

        // false == do NOT stop the simulation
        return false;
    }
}
