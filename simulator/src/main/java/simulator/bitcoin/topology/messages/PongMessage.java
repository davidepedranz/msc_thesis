package simulator.bitcoin.topology.messages;

import peersim.core.Node;

/**
 * Bitcoin `pong` message: it is used to answer a {@link PingMessage}.
 * See: https://bitcoin.org/en/developer-reference#pong
 */
public final class PongMessage {

	public final Node sender;

	public PongMessage(Node sender) {
		this.sender = sender;
	}
}
