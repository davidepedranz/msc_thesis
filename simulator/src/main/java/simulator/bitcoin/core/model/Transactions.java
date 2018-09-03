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

import simulator.bitcoin.core.initializers.TransactionsInitializer;

import java.util.ArrayList;
import java.util.List;

/**
 * Global storage all ALL generated transactions. This class is populated at the beginning
 * for the simulation by {@link TransactionsInitializer}. The store contains all transactions,
 * even before they are scheduled to the nodes.
 */
public final class Transactions {

    // keep track of all transactions, in order of generation
    public static final List<Transaction> TRANSACTION_LIST = new ArrayList<>();

    // assign to each transaction a different progressive ID
    private static int transactionCounter = 0;

    // prevent class construction
    private Transactions() {
    }

    /**
     * Get the transaction with ID i.
     *
     * @param i ID of the transaction to getBlock.
     * @return Transaction with the given index.
     */
    public static Transaction getTransaction(int i) {
        return TRANSACTION_LIST.get(i);
    }

    /**
     * Generate a new transaction with the given timestamp and adds it to the store.
     *
     * @param timestamp Transaction creation timestamp.
     * @return Newly generated transaction.
     */
    public static Transaction nextTransaction(long timestamp) {
        final Transaction transaction = new Transaction(transactionCounter, timestamp);
        TRANSACTION_LIST.add(transaction);
        transactionCounter++;
        return transaction;
    }
}
