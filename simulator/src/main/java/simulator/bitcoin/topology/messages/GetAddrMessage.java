package simulator.bitcoin.topology.messages;

import peersim.core.Node;

/**
 * Bitcoin `GetAddr` message: it is used to request a list of peers to another node.
 * See: https://bitcoin.org/en/developer-reference#getaddr
 */
public final class GetAddrMessage {

	public final Node sender;

	public GetAddrMessage(Node sender) {
		this.sender = sender;
	}
}
