package simulator.observers;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;
import peersim.util.IncrementalFreq;

/**
 * Observer that tracks the structure of the blockchain generated by the protocol.
 */
public final class BlockchainObserver implements Control {

	// parameters
	private static final String PARAMETER_PROTOCOL = "protocol";

	// fields
	private final String name;
	private final int pid;

	public BlockchainObserver(String name) {
		this.name = name;
		this.pid = Configuration.getPid(name + "." + PARAMETER_PROTOCOL);
	}

	@Override
	public boolean execute() {

		// compute the statistics over all nodes in the network
		// NB: we count the TOTALS, so please divide by the number of nodes in the simulation to get the mean
		final IncrementalFreq stats = new IncrementalFreq();
		for (int i = 0; i < Network.size(); i++) {
			final BlockchainMetric protocol = (BlockchainMetric) Network.get(i).getProtocol(pid);
			final int[] values = protocol.blockchain().forksLengths();
			for (int value : values) {
				stats.add(value);
			}
		}

		// print them out, following Peersim conventions
		System.out.println(name + ": [" + CommonState.getTime() + "] " + stats);

		// false == do NOT stop the simulation
		return false;
	}
}
