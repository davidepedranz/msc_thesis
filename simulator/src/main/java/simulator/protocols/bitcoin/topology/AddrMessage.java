package simulator.protocols.bitcoin.topology;

import peersim.core.Node;

/**
 * Bitcoin `addr` message: it is used to push a list of peers to another node.
 * See: https://bitcoin.org/en/developer-reference#addr
 */
final class AddrMessage {

	final Node[] peers;

	AddrMessage(Node[] peers) {
		this.peers = peers;
	}
}
