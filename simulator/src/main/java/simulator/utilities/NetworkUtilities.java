package simulator.utilities;

import peersim.config.FastConfig;
import peersim.core.Linkable;
import peersim.core.Node;
import peersim.transport.Transport;

/**
 * @author Davide Pedranz (pedranz@fbk.eu)
 */
public final class NetworkUtilities {

	// TODO: connections symmetric TCP


	public static void reply(Node from, Node to, int pid, Object message) {
		final Linkable linkable = (Linkable) from.getProtocol(FastConfig.getLinkable(pid));
		assert linkable.contains(to);
		final Transport transport = (Transport) from.getProtocol(FastConfig.getTransport(pid));
		transport.send(from, to, message, pid);
	}

	public static void broadcast(Node from, int pid, Object message) {
		final Linkable linkable = (Linkable) from.getProtocol(FastConfig.getLinkable(pid));
		final Transport transport = (Transport) from.getProtocol(FastConfig.getTransport(pid));
		for (int i = 0; i < linkable.degree(); i++) {
			if (from.getIndex() != i) {
				transport.send(from, linkable.getNeighbor(i), message, pid);
			}
		}
	}
}
