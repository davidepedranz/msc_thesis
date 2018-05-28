package simulator.protocols.bitcoin.topology.events;

import simulator.protocols.bitcoin.topology.BitcoinTopology;

/**
 * Event used to schedule a pong timeout, which is used to cleanup dead nodes
 * from the Bitcoin topology. See {@link BitcoinTopology}.
 */
public final class SchedulePongTimeouts {
	public static final SchedulePongTimeouts INSTANCE = new SchedulePongTimeouts();

	private SchedulePongTimeouts() {
	}
}
