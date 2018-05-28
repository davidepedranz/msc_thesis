package simulator.observers;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;
import peersim.util.IncrementalStats;

/**
 * Base class that allows to monitor a single parameter of a protocol.
 */
abstract class BaseObserver implements Control {

	// parameters
	private static final String PARAMETER_PROTOCOL = "protocol";

	// fields
	private final String name;
	private final int pid;

	BaseObserver(String name) {
		this.name = name;
		this.pid = Configuration.getPid(name + "." + PARAMETER_PROTOCOL);
	}

	@Override
	public boolean execute() {

		// compute the statistics over all nodes in the network
		final IncrementalStats stats = new IncrementalStats();
		for (int i = 0; i < Network.size(); i++) {
			final double value = getValue(i, pid);
			stats.add(value);
		}

		// print them out, following Peersim conventions
		System.out.println(name + ": [" + CommonState.getTime() + "] " + stats);

		// false == do NOT stop the simulation
		return false;
	}

	/**
	 * Get the value to observe from a protocol.
	 *
	 * @param index Index of the node from which to get the value.
	 * @param pid   ID of the protocol.
	 * @return The value to observe.
	 */
	abstract double getValue(int index, int pid);
}
