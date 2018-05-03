package simulator.protocols.bitcoin.topology;

import peersim.core.Node;

/**
 * Bitcoin `pong` message: it is used to answer a {@link PingMessage}.
 * See: https://bitcoin.org/en/developer-reference#pong
 */
final class PongMessage {

	final Node sender;

	PongMessage(Node sender) {
		this.sender = sender;
	}
}
