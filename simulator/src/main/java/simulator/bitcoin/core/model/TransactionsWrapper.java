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
 * Container of transactions that fit in one {@link Block}.
 * This class is used for performances reasons. It always allocate the maximum number of
 * transactions that can be contained in a single Block and keeps count of the real number.
 */
public final class TransactionsWrapper {

    public final Transaction[] transactions;
    public int transactionsNumber;

    public TransactionsWrapper(int blockSize) {
        this.transactions = new Transaction[blockSize];
        this.transactionsNumber = 0;
    }

    TransactionsWrapper(Transaction[] transactions) {
        this.transactions = transactions;
        this.transactionsNumber = transactions.length;
    }
}
