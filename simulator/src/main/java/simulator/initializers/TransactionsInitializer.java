package simulator.initializers;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import simulator.model.Transaction;
import simulator.utilities.GlobalState;

import static simulator.utilities.NetworkUtilities.scheduleEventForAllNodes;

/**
 * {@link Control} used to simulate external users of the protocol that
 * generate {@link Transaction}s to be stored in the public ledger.
 */
public final class TransactionsInitializer implements Control {

	// parameters
	private static final String PARAMETER_MEAN = "mean";
	private static final String PARAMETER_PROTOCOL = "protocol";

	// fields
	private final int mean;
	private final int pid;

	public TransactionsInitializer(String name) {
		this.mean = Configuration.getInt(name + "." + PARAMETER_MEAN);
		this.pid = Configuration.getPid(name + "." + PARAMETER_PROTOCOL);
	}

	@Override
	public boolean execute() {

		// generate and schedule transactions for the entire simulation
		// transactions are stored globally and available to all nodes
		// as soon as they will receive the corresponding event
		long delay = 0;
		while (delay < CommonState.getEndTime()) {
			final long delta = CommonState.r.nextPoisson(mean);
			delay += delta;
			scheduleTransaction(delay);
		}

		// false == do NOT stop the simulation
		return false;
	}

	private void scheduleTransaction(long delay) {
		final Transaction transaction = GlobalState.nextTransaction();
		scheduleEventForAllNodes(delay, transaction, pid);
	}
}
