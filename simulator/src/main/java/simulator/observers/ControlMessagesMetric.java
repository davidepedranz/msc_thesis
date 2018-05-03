package simulator.observers;

/**
 * Interface to be implemented by {@link peersim.core.Protocol}s that measure
 * the number of control messages used to run some sub-part of the protocol
 * used by the main one, for example construct the p2p network topology.
 */
public interface ControlMessagesMetric {

	/**
	 * @param nodeIndex Index of the node.
	 * @return Number of control messages used by the protocol.
	 */
	long controlMessages(int nodeIndex);
}
