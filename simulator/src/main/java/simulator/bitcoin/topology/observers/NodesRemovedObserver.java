package simulator.bitcoin.topology.observers;

import peersim.core.Network;
import simulator.bitcoin.topology.BitcoinTopology;
import simulator.utilities.peersim.BaseObserver;

/**
 * Observer of the number of nodes removed because of pong timeouts in the {@link BitcoinTopology} protocol.
 */
public final class NodesRemovedObserver extends BaseObserver {

	public NodesRemovedObserver(String name) {
		super(name);
	}

	@Override
	protected double getValue(int index, int pid) {
		final BitcoinTopology protocol = (BitcoinTopology) Network.get(index).getProtocol(pid);
		return protocol.nodesRemovedForPongTimeout;
	}
}
