package simulator.protocols.bitcoin.transactions.events;

import com.google.auto.value.AutoValue;
import simulator.model.Block;

/**
 * Event that represent the discovery of a new block,
 * eg. end of the computation for the block hash in Bitcoin.
 */
@AutoValue
public abstract class BlockFoundEvent {

	// TODO: replace Block with something else, so that blocks are only generated when the mining finishes!!!
	public static BlockFoundEvent create(long miningStartTime, Block block) {
		return new AutoValue_BlockFoundEvent(miningStartTime, block);
	}

	public abstract long miningStartTime();

	public abstract Block block();
}
