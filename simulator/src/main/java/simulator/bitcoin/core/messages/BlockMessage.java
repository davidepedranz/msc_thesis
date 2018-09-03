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

import simulator.bitcoin.core.model.Block;

/**
 * Bitcoin `Block` message (@see <a href="https://bitcoin.org/en/developer-reference#block">Documentation</a>)
 * It is used to send a single {@link simulator.bitcoin.core.model.Block} object to a peer. It can be
 * the reply to a {@link GetDataMessage} or sent unsolicited when a new block is generated.
 */
public final class BlockMessage {

    public final Block block;

    public BlockMessage(Block block) {
        this.block = block;
    }
}
