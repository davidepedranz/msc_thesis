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

package simulator.bitcoin.core.messages;

import peersim.core.Node;

/**
 * Bitcoin `Inv` message (@see <a href="https://bitcoin.org/en/developer-reference#inv">Documentation</a>)
 * It can be the response to a {@link GetBlocksMessage} or MemPoolMessage, or can also be send
 * unsolicited to announce the presence of new blocks or transactions. It contains the headers
 * of blocks or transactions.
 * <p>
 * NB: Since we are interested in the forks on the blockchain, we simplify the broadcast of
 * transactions in the simulation, so the `Inv` message is only used for blocks.
 */
public final class InvMessage {

    public final Node sender;
    public final int[] headers;

    public InvMessage(Node sender, int[] headers) {
        this.sender = sender;
        this.headers = headers;
    }
}
