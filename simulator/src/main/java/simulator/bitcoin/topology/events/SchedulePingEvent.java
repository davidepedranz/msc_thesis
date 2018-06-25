package simulator.bitcoin.topology.events;

import simulator.bitcoin.topology.BitcoinTopology;

/**
 * Event used to schedule a ping. See {@link BitcoinTopology}.
 */
public final class SchedulePingEvent {

	public static final SchedulePingEvent INSTANCE = new SchedulePingEvent();

	private SchedulePingEvent() {
	}
}
