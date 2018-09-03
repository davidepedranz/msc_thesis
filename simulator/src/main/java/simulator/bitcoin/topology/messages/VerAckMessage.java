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
 * Bitcoin `VerAck` message: acknowledges a previously-received version message.
 * See: https://bitcoin.org/en/developer-reference#verack
 */
public final class VerAckMessage {

    public final Node sender;

    public VerAckMessage(Node sender) {
        this.sender = sender;
    }
}
