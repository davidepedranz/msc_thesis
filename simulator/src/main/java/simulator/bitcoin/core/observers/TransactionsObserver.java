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
import peersim.util.IncrementalStats;
import simulator.bitcoin.core.model.*;

import java.util.function.BiFunction;

import static simulator.collections.ArrayUtilities.binarySearch;

/**
 * Observer that measures the time needed to store a transaction on the main branch
 * of the blockchain. NB: the same transaction can be in more than one chain.
 */
public final class TransactionsObserver implements Control {

    // fields
    private final String prefix;

    /**
     * Default constructor, following the PeerSim conventions.
     * The prefix field is automatically provided by the PeerSim engine.
     *
     * @param prefix Prefix of this control in the configuration file.
     */
    public TransactionsObserver(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public boolean execute() {
        final IncrementalStats stats = new IncrementalStats();

        // find the index of the last transaction seen so far
        final BiFunction<Transaction, Long, Boolean> comparator = (tx, timestamp) -> tx.timestamp <= timestamp;
        final int lastTransactionIndex = binarySearch(Transactions.TRANSACTION_LIST, CommonState.getTime(), comparator);

        // nothing to compute if there are no transactions issued before the maximum timestamp
        if (lastTransactionIndex >= 0) {

            // compute the time needed to enter a block in the blockchain
            final long[] times = new long[lastTransactionIndex + 1];
            Block block = Blocks.getLongestChain();
            while (block != null) {
                final TransactionsWrapper wrapper = block.transactions;
                for (int i = 0; i < wrapper.transactionsNumber; i++) {
                    final Transaction transaction = wrapper.transactions[i];
                    final long delta = block.timestamp - transaction.timestamp;
                    times[transaction.id] = delta;
                }
                block = block.previous;
            }

            // compute the statistics over all transactions generated so far...
            for (long time : times) {
                // we do not count the transactions that are not yet present in one block!
                if (time > 0) {
                    stats.add(time);
                }
            }
        }

        // print them out, following PeerSim conventions
        System.out.println(prefix + ": [" + CommonState.getTime() + "] " + stats);

        // false == do NOT stop the simulation
        return false;
    }
}
