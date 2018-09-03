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
import simulator.collections.IntList;

/**
 * Bitcoin `GetData` message (@see <a href="https://bitcoin.org/en/developer-reference#getdata">Documentation</a>)
 * It is used to requests specific data contained in an {@link InvMessage} (with blocks or transactions) to a peer.
 * <p>
 * NB: Since we are interested in the forks on the blockchain, we simplify the broadcast of
 * transactions in the simulation, so the `GetData` message is only used for blocks.
 */
public final class GetDataMessage {

    public final Node sender;
    public final IntList headers;

    public GetDataMessage(Node sender, IntList headers) {
        this.sender = sender;
        this.headers = headers;
    }
}
