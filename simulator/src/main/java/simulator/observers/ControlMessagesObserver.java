package simulator.observers;

import peersim.core.Network;

/**
 * Observer that collects the number of control messages for the protocol among all nodes of the network.
 */
public final class ControlMessagesObserver extends BaseObserver {

	public ControlMessagesObserver(String name) {
		super(name);
	}

	@Override
	double getValue(int index, int pid) {
		final ControlMessagesMetric protocol = (ControlMessagesMetric) Network.get(index).getProtocol(pid);
		return protocol.controlMessages(index);
	}
}
