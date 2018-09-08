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

import peersim.utilities.AbstractObserver;
import simulator.bitcoin.core.model.Blocks;

/**
 * Observer that tracks the number of blocks generated during the simulation.
 */
public final class BlocksObserverObserver extends AbstractObserver {

    /**
     * Default constructor, following the PeerSim conventions.
     * The prefix field is automatically provided by the PeerSim engine.
     *
     * @param prefix Prefix of this control in the configuration file.
     */
    public BlocksObserverObserver(String prefix) {
        super(prefix);
    }

    @Override
    protected double getValue(int index, int pid) {
        return Blocks.getBlocksCount();
    }
}
