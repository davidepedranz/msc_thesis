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
