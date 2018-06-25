package simulator.bitcoin.core.messages;

import simulator.bitcoin.core.model.Block;

/**
 * Bitcoin `Block` message (@see <a href="https://bitcoin.org/en/developer-reference#block">Documentation</a>)
 * It is used to send a single {@link simulator.bitcoin.core.model.Block} object to a peer. It can be the reply to a
 * {@link GetDataMessage} or sent unsolicited when a new block is generated.
 */
public final class BlockMessage {

	public final Block block;

	public BlockMessage(Block block) {
		this.block = block;
	}
}
