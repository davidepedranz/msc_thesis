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
 * Bitcoin `GetBlocks` message (@see <a href="https://bitcoin.org/en/developer-reference#getblocks">Documentation</a>)
 * It is used to request an {@link InvMessage} to a peer node. This is useful for new peers to sync the blockchain or
 * for disconnected ones to update their status quickly.
 */
public final class GetBlocksMessage {

    public final Node sender;

    // NB: in the real protocol, block headers are cryptographic hashes
    // in our simulation we will just use block numbers
    public final int[] blockHeaders;

    public GetBlocksMessage(Node sender, int[] blockHeaders) {
        this.sender = sender;
        this.blockHeaders = blockHeaders;
    }
}
