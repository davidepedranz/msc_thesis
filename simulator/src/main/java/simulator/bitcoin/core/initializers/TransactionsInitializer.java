/*
 * Copyright (c) 2018 Davide Pedranz. All rights reserved.
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package simulator.bitcoin.core.initializers;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.utilities.Distributions;
import simulator.bitcoin.core.messages.TxMessage;
import simulator.bitcoin.core.model.Transaction;
import simulator.bitcoin.core.model.Transactions;

import static peersim.utilities.ConfigurationHelper.readBoolean;
import static peersim.utilities.ConfigurationHelper.readPid;
import static peersim.utilities.SimulationUtilities.scheduleEventForRandomNode;

/**
 * {@link Control} used to simulate external users of the protocol that
 * generate {@link Transaction}s to be stored in the public ledger.
 * Each transaction will be scheduled to EXACTLY one node in the network.
 */
public final class TransactionsInitializer implements Control {

    // parameters
    private static final String PARAM_MEAN = "mean";
    private static final String PARAM_ENABLE = "enable";

    // fields
    private final int mean;
    private final int pid;
    private final boolean enable;

    /**
     * Default constructor, following the PeerSim conventions.
     * The prefix field is automatically provided by the PeerSim engine.
     *
     * @param prefix Prefix of this control in the configuration file.
     */
    public TransactionsInitializer(String prefix) {
        this.mean = Configuration.getInt(prefix + "." + PARAM_MEAN);
        this.pid = readPid(prefix);
        this.enable = readBoolean(prefix, PARAM_ENABLE, true);
    }

    @Override
    public boolean execute() {

        // keep track of the number of generated transactions
        int count = 0;

        // generate and schedule transactions for the entire simulation
        // transactions are stored globally and available to all nodes
        // as soon as they will receive the corresponding event
        long delay = 0;
        while (delay < CommonState.getEndTime()) {
            final long delta = Distributions.nextExponentialRounded(mean);
            delay += delta;
            count++;

            // optionally disable transactions... this speeds up the simulation a lot
            if (enable) {
                final Transaction transaction = Transactions.nextTransaction(delay);
                scheduleEventForRandomNode(delay, new TxMessage(transaction), pid);
            }
        }

        // log the number of simulated transactions
        System.err.println("Generated " + count + " transaction...");

        // false == do NOT stop the simulation
        return false;
    }
}
