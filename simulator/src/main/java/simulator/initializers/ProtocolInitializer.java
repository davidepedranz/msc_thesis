package simulator.initializers;

import peersim.config.Configuration;
import peersim.core.Control;
import simulator.events.StartEvent;

import static simulator.utilities.NetworkUtilities.scheduleEventForAllNodes;

/**
 * {@link Control} used to initialize a protocol, using a {@link StartEvent}.
 */
public final class ProtocolInitializer implements Control {

	// parameters
	private static final String PARAMETER_PROTOCOL = "protocol";

	// fields
	private final int pid;

	public ProtocolInitializer(String name) {
		this.pid = Configuration.getPid(name + "." + PARAMETER_PROTOCOL);
	}

	@Override
	public boolean execute() {

		// schedule the start event immediately == time zero
		final StartEvent startEvent = StartEvent.INSTANCE;
		scheduleEventForAllNodes(0, startEvent, pid);

		// false == do NOT stop the simulation
		return false;
	}
}
