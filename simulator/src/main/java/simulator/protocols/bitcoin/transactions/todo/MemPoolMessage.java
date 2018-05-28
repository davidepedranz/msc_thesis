package simulator.protocols.bitcoin.transactions.todo;

import peersim.core.Node;
import simulator.protocols.bitcoin.transactions.messages.InvMessage;

/**
 * Bitcoin `MemPool` message (@see <a href="https://bitcoin.org/en/developer-reference#mempool">Documentation</a>)
 * It is used to request an {@link InvMessage} with the list of pending transactions to a peer node.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
final class MemPoolMessage {

	final Node sender;

	MemPoolMessage(Node sender) {
		this.sender = sender;
	}
}
