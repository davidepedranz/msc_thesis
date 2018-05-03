package simulator.observers;

import peersim.core.Network;

/**
 * Observer that collects CPU usage metrics among all nodes of the network.
 */
public final class CPUObserver extends BaseObserver {

	public CPUObserver(String name) {
		super(name);
	}

	@Override
	double getValue(int index, int pid) {
		final CPUMetric protocol = (CPUMetric) Network.get(index).getProtocol(pid);
		return protocol.cpuTime(index);
	}
}
