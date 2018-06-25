package simulator.bitcoin.topology.messages;

import peersim.core.Node;

/**
 * Bitcoin `Version` message: first message exchanged between peers.
 * See: https://bitcoin.org/en/developer-reference#version
 */
public final class VersionMessage {

	public final Node sender;

	public VersionMessage(Node sender) {
		this.sender = sender;
	}
}
