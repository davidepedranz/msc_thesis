package simulator.events;

/**
 * Event used to signal a protocol to start,
 * eg. {@link simulator.initializers.ProtocolInitializer}.
 */
public final class StartEvent {
	public static final StartEvent INSTANCE = new StartEvent();

	private StartEvent() {
	}
}
