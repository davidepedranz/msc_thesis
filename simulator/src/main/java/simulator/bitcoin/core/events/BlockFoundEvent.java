package simulator.bitcoin.core.events;

import simulator.bitcoin.core.model.Block;
import simulator.bitcoin.core.model.TransactionsWrapper;

/**
 * Event that represent the discovery of a new block, eg. end of the computation for the block hash in Bitcoin.
 */
@SuppressWarnings("WeakerAccess")
public final class BlockFoundEvent {

	public final long miningStartTime;
	public final Block previous;
	public final TransactionsWrapper transactions;

	public BlockFoundEvent(long miningStartTime, Block previous, TransactionsWrapper transactions) {
		this.miningStartTime = miningStartTime;
		this.previous = previous;
		this.transactions = transactions;
	}
}
