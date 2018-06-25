package simulator.bitcoin.core.messages;

import peersim.core.Node;
import simulator.utilities.structures.IntQueue;

/**
 * Bitcoin `GetData` message (@see <a href="https://bitcoin.org/en/developer-reference#getdata">Documentation</a>)
 * It is used to requests specific data contained in an {@link InvMessage} (blocks or transactions) to a peer.
 */
public final class GetDataMessage {

	public final Node sender;
	public final Type type;
	public final IntQueue headers;

	public GetDataMessage(Node sender, Type type, IntQueue headers) {
		this.sender = sender;
		this.type = type;
		this.headers = headers;
	}
}
