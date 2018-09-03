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

package simulator.bitcoin.topology.messages;

import peersim.core.Node;

/**
 * Bitcoin `GetAddr` message: it is used to request a list of peers to another node.
 * See: https://bitcoin.org/en/developer-reference#getaddr
 */
public final class GetAddrMessage {

    public final Node sender;

    public GetAddrMessage(Node sender) {
        this.sender = sender;
    }
}
