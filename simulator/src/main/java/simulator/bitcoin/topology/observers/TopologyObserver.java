package simulator.bitcoin.topology.observers;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;
import peersim.util.IncrementalFreq;
import simulator.bitcoin.topology.BitcoinTopology;

/**
 * Observe the topology created by Bitcoin nodes in the {@link BitcoinTopology} protocol.
 */
public final class TopologyObserver implements Control {

	// parameters
	private static final String PARAMETER_PROTOCOL = "protocol";

	// fields
	private final String name;
	private final int pid;

	public TopologyObserver(String name) {
		this.name = name;
		this.pid = Configuration.getPid(name + "." + PARAMETER_PROTOCOL);
	}

	@Override
	public boolean execute() {

		// compute the statistics over all nodes in the network
		final IncrementalFreq outgoing = new IncrementalFreq();
		final IncrementalFreq incoming = new IncrementalFreq();
		final IncrementalFreq peers = new IncrementalFreq();
		for (int i = 0; i < Network.size(); i++) {
			final BitcoinTopology protocol = (BitcoinTopology) Network.get(i).getProtocol(pid);
			outgoing.add(protocol.degreeOutgoing());
			incoming.add(protocol.degreeIncoming());
			peers.add(protocol.peers());
		}

		// print them out, following Peersim conventions
		System.out.println(name + "-out: [" + CommonState.getTime() + "] " + outgoing);
		System.out.println(name + "-in: [" + CommonState.getTime() + "] " + incoming);
		System.out.println(name + "-peers: [" + CommonState.getTime() + "] " + peers);

		// false == do NOT stop the simulation
		return false;
	}
}
