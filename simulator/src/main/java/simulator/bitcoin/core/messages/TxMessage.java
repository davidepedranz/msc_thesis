package simulator.bitcoin.core.messages;

import simulator.bitcoin.core.model.Transaction;

/**
 * Bitcoin `Tx` message (@see <a href="https://bitcoin.org/en/developer-reference#tx">Documentation</a>)
 * It is used to send a single {@link simulator.bitcoin.core.model.Transaction} object to a peer. It can be the reply
 * to a {@link GetDataMessage} or sent unsolicited when a new transaction is generated / received.
 */
public final class TxMessage {

	public final Transaction transaction;

	public TxMessage(Transaction transaction) {
		this.transaction = transaction;
	}
}
