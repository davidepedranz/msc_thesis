package simulator.bitcoin.topology.messages;

import peersim.core.Node;

/**
 * Bitcoin `ping` message: it is used to maintain the list of peers.
 * See: https://bitcoin.org/en/developer-reference#ping
 */
public final class PingMessage {

	public final Node sender;

	public PingMessage(Node sender) {
		this.sender = sender;
	}
}
