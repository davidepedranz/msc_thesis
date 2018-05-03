package simulator.protocols.bitcoin.topology;

/**
 * Event used to simulate the random delay in scheduling the `addr`
 * periodic message in Bitcoin.
 */
public final class ScheduleAddrEvent {
	public static final ScheduleAddrEvent INSTANCE = new ScheduleAddrEvent();
}
