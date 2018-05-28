package simulator.protocols.bitcoin.transactions.messages;

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
