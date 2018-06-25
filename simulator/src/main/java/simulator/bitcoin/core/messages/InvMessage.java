package simulator.bitcoin.core.messages;

import peersim.core.Node;

/**
 * Bitcoin `Inv` message (@see <a href="https://bitcoin.org/en/developer-reference#inv">Documentation</a>)
 * It can be the response to a {@link GetBlocksMessage} or MemPoolMessage, or can also be send
 * unsolicited to announce the presence of new blocks or transactions. It contains the headers of blocks
 * or transactions.
 */
public final class InvMessage {

	public final Node sender;
	public final Type type;
	public final int[] headers;

	public InvMessage(Node sender, Type type, int[] headers) {
		this.sender = sender;
		this.type = type;
		this.headers = headers;
	}
}
