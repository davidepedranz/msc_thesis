package simulator.bitcoin.topology.events;

/**
 * Event used to simulate the random delay in scheduling the `addr`
 * periodic message in Bitcoin.
 */
public final class ScheduleQueueAddrEvent {

	public static final ScheduleQueueAddrEvent INSTANCE = new ScheduleQueueAddrEvent();

	private ScheduleQueueAddrEvent() {
	}
}
