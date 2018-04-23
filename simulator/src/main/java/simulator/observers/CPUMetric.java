package simulator.observers;

/**
 * Interface to be implemented by {@link peersim.core.Protocol}s that measure
 * the CPU usage of the nodes. This is useful to quantify the cost of PoW.
 */
public interface CPUMetric {

	/**
	 * @return Number of time units where the CPU was aggressively used by the protocol.
	 */
	long cpuTime();
}
