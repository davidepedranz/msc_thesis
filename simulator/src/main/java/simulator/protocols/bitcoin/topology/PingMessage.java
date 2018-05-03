package simulator.protocols.bitcoin.topology;

import peersim.core.Node;

/**
 * Bitcoin `ping` message: it is used to maintain the list of peers.
 * See: https://bitcoin.org/en/developer-reference#ping
 */
final class PingMessage {

	final Node sender;

	PingMessage(Node sender) {
		this.sender = sender;
	}
}
