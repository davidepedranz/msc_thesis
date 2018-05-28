package simulator.utilities;

import peersim.core.CommonState;
import peersim.core.Network;
import peersim.core.Node;
import peersim.edsim.EDSimulator;

/**
 * Class of utilities to interact with the {@link peersim.core.Network}
 * and {@link peersim.edsim.EDSimulator} objects of Peersim.
 */
public final class SimulationUtilities {

	/**
	 * Schedules the given event to one random node that implements the given protocol.
	 *
	 * @param delay Delay (time units from now) at which to schedule the event.
	 * @param event Event to schedule to ALL nodes implementing the protocol.
	 * @param pid   ID of the protocol that will receive the given event.
	 */
	public static void scheduleEventForRandomNode(long delay, Object event, int pid) {
		final int nodeIndex = CommonState.r.nextInt(Network.size());
		EDSimulator.add(delay, event, Network.get(nodeIndex), pid);
	}

	/**
	 * Schedules the given event to ALL nodes that implements the given protocol.
	 *
	 * @param delay Delay (time units from now) at which to schedule the event.
	 * @param event Event to schedule to ALL nodes implementing the protocol.
	 * @param pid   ID of the protocol that will receive the given event.
	 */
	public static void scheduleEventForAllNodes(long delay, Object event, int pid) {
		for (int i = 0; i < Network.size(); i++) {
			final Node node = Network.get(i);
			EDSimulator.add(delay, event, node, pid);
		}
	}
}