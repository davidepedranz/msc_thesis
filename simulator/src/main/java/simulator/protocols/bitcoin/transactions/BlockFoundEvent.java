package simulator.protocols.bitcoin.transactions;

import com.google.auto.value.AutoValue;
import simulator.model.Block;

/**
 * Event that represent the discovery of a new block,
 * eg. end of the computation for the block hash in Bitcoin.
 */
@AutoValue
abstract class BlockFoundEvent {

	static BlockFoundEvent create(long miningStartTime, Block block) {
		return new AutoValue_BlockFoundEvent(miningStartTime, block);
	}

	abstract long miningStartTime();

	abstract Block block();
}
