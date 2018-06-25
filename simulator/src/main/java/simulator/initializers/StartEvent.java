package simulator.initializers;

/**
 * Event used to signal a protocol to start,
 * eg. {@link simulator.initializers.ProtocolInitializer}.
 */
public final class StartEvent {

	static final StartEvent INSTANCE = new StartEvent();

	private StartEvent() {
	}
}
