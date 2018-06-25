package simulator.bitcoin.topology.observers;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;
import peersim.util.IncrementalStats;
import simulator.bitcoin.topology.BitcoinTopology;

/**
 * Observer of the number of control messages used by the {@link BitcoinTopology} protocol.
 */
public final class MessagesObserver implements Control {

	// parameters
	private static final String PARAMETER_PROTOCOL = "protocol";

	// fields
	private final String name;
	private final int pid;

	public MessagesObserver(String name) {
		this.name = name;
		this.pid = Configuration.getPid(name + "." + PARAMETER_PROTOCOL);
	}

	@Override
	public boolean execute() {

		// compute the statistics over all nodes in the network
		final IncrementalStats versionMessages = new IncrementalStats();
		final IncrementalStats verAckMessages = new IncrementalStats();
		final IncrementalStats getAddrMessages = new IncrementalStats();
		final IncrementalStats addrMessages = new IncrementalStats();
		final IncrementalStats pingMessages = new IncrementalStats();
		final IncrementalStats pongMessages = new IncrementalStats();
		for (int i = 0; i < Network.size(); i++) {
			final BitcoinTopology protocol = (BitcoinTopology) Network.get(i).getProtocol(pid);
			versionMessages.add(protocol.versionMessages);
			verAckMessages.add(protocol.verAckMessages);
			getAddrMessages.add(protocol.getAddrMessages);
			addrMessages.add(protocol.addrMessages);
			pingMessages.add(protocol.pingMessages);
			pongMessages.add(protocol.pongMessages);
		}

		// print them out, following Peersim conventions
		System.out.println(name + "-version: [" + CommonState.getTime() + "] " + versionMessages);
		System.out.println(name + "-verAck: [" + CommonState.getTime() + "] " + verAckMessages);
		System.out.println(name + "-getAddr: [" + CommonState.getTime() + "] " + getAddrMessages);
		System.out.println(name + "-addr: [" + CommonState.getTime() + "] " + addrMessages);
		System.out.println(name + "-ping: [" + CommonState.getTime() + "] " + pingMessages);
		System.out.println(name + "-pong: [" + CommonState.getTime() + "] " + pongMessages);

		// false == do NOT stop the simulation
		return false;
	}
}
