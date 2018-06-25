package simulator.bitcoin.topology.events;

import peersim.core.Node;

public final class ScheduleConnectTimeoutEvent {

	public final Node peer;

	public ScheduleConnectTimeoutEvent(Node peer) {
		this.peer = peer;
	}
}
