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

package simulator.bitcoin.core.model;

/**
 * Models a single block in the Bitcoin protocol.
 */
public final class Block {

    // immutable fields
    public final int id;
    final int height;
    public final Block previous;
    public final TransactionsWrapper transactions;
    public final long timestamp;

    // mutable fields
    Block[] children;
    int childrenNumber;

    Block(int id, int height, Block previous, TransactionsWrapper transactions, long timestamp) {
        this.id = id;
        this.height = height;
        this.previous = previous;
        this.transactions = transactions;
        this.timestamp = timestamp;
        this.children = new Block[1];
        this.childrenNumber = 0;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Block block = (Block) o;
        return id == block.id;
    }

    @Override
    public String toString() {
        final Integer previousId = previous != null ? previous.id : null;
        return String.format("Block{id=%s, height=%s, previous=%s, timestamp=%s}", id, height, previousId, timestamp);
    }
}
