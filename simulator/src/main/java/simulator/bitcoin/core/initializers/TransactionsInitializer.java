package simulator.bitcoin.core.initializers;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import simulator.bitcoin.core.messages.TxMessage;
import simulator.bitcoin.core.model.Transaction;
import simulator.bitcoin.core.model.Transactions;
import simulator.utilities.peersim.Distributions;

import static simulator.utilities.peersim.SimulationUtilities.scheduleEventForRandomNode;

/**
 * {@link Control} used to simulate external users of the protocol that
 * generate {@link Transaction}s to be stored in the public ledger.
 * Each transaction will be scheduled to EXACTLY one node in the network.
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
			final long delta = Distributions.roundedNextExponential(mean);
			delay += delta;
			scheduleTransaction(delay);
		}

		// false == do NOT stop the simulation
		return false;
	}

	private void scheduleTransaction(long delay) {
		final Transaction transaction = Transactions.nextTransaction(delay);
		scheduleEventForRandomNode(delay, new TxMessage(transaction), pid);
	}
}
