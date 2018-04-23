package simulator.observers;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;
import peersim.util.IncrementalStats;

/**
 * Observer that collects CPU usage metrics among all nodes of the network.
 */
public final class CPUObserver implements Control {

	// parameters
	private static final String PARAMETER_PROTOCOL = "protocol";

	// fields
	private final String name;
	private final int pid;

	public CPUObserver(String name) {
		this.name = name;
		this.pid = Configuration.getPid(name + "." + PARAMETER_PROTOCOL);
	}

	@Override
	public boolean execute() {

		// compute the statistics over all nodes in the network
		final IncrementalStats stats = new IncrementalStats();
		for (int i = 0; i < Network.size(); i++) {
			final CPUMetric protocol = (CPUMetric) Network.get(i).getProtocol(pid);
			stats.add(protocol.cpuTime());
		}

		// print them out, following Peersim conventions
		System.out.println(name + ": [" + CommonState.getTime() + "] " + stats);

		// false == do NOT stop the simulation
		return false;
	}
}
