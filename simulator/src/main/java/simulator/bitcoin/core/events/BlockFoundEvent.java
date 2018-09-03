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

package simulator.bitcoin.core.events;

import simulator.bitcoin.core.model.Block;
import simulator.bitcoin.core.model.TransactionsWrapper;

/**
 * Event that represent the discovery of a new block,
 * eg. end of the computation for the block hash in Bitcoin.
 */
public final class BlockFoundEvent {

    public final Block previous;
    public final TransactionsWrapper transactions;

    public BlockFoundEvent(Block previous, TransactionsWrapper transactions) {
        this.previous = previous;
        this.transactions = transactions;
    }
}
