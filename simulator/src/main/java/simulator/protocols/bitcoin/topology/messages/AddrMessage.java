package simulator.protocols.bitcoin.topology.messages;

import peersim.core.Node;

/**
 * Bitcoin `addr` message: it is used to push a list of peers to another node.
 * See: https://bitcoin.org/en/developer-reference#addr
 */
public final class AddrMessage {

	public final Node[] peers;

	public AddrMessage(Node[] peers) {
		this.peers = peers;
	}
}
