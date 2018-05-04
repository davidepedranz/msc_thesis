package simulator.observers;

import simulator.model.Blockchain;

/**
 * Interface to be implemented by {@link peersim.core.Protocol}s that measure
 * the number of forks seen by each node. This is useful to quantify the cost of PoW.
 */
public interface BlockchainMetric {

	/**
	 * @return Number of forks in the blockchain.
	 */
	Blockchain blockchain();
}
