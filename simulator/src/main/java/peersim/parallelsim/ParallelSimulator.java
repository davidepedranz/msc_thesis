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

package peersim.parallelsim;

import peersim.Simulator;
import peersim.config.Configuration;
import peersim.config.ParsedProperties;
import peersim.rangesim.TaggedOutputStream;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility class that parses the configuration file of PeerSim and generates
 * the command line options for each combination of the ranges parameters,
 * ready to be passed to the {@link Simulator} class.
 * <p>
 * This class is useful to run multiple simulations in parallel using tools
 * such as GNU Parallel (https://www.gnu.org/software/parallel/).
 */
public final class ParallelSimulator {

    /**
     * Standard exit code used by PeerSim on errors.
     */
    private static final int PEERSIM_ERROR_EXIT_CODE = 101;

    /**
     * Prefix of the config properties whose value vary during a set of experiments.
     */
    private static final String PARAM_RANGE = "range";

    /**
     * Entry point.
     *
     * @param args Command line parameters.
     */
    public static void main(String[] args) {

        // check the command line parameters
        if (args.length != 1) {
            System.err.println("Usage: simulator.ParallelSimulator [config.file]");
            System.exit(PEERSIM_ERROR_EXIT_CODE);
        }

        // load the configuration file
        // NB: for some reason, we MUST CLONE the arguments!
        final Properties properties = new ParsedProperties(args.clone());
        Configuration.setConfig(properties);

        // compute the argument common to each simulation
        final List<String> commonArguments = commonArguments(args);

        // load the ranges
        final String[] rangeNames = Configuration.getNames(PARAM_RANGE);
        final Ranges ranges = new Ranges(rangeNames);

        // iterate all possible combinations of parameters
        final Iterator<String[]> iterator = ranges.iterator();
        while (iterator.hasNext()) {
            final String[] currentParameters = iterator.next();

            // arguments for this simulation ... with the common arguments
            final List<String> arguments = new ArrayList<>(commonArguments);

            // setup the log
            final List<String> argumentsWithSpace = Arrays.stream(currentParameters)
                .map(string -> string.replace("=", " "))
                .collect(Collectors.toList());
            final String logPrefix = String.join(" ", argumentsWithSpace);
            arguments.add(Simulator.PAR_REDIRECT + "." + TaggedOutputStream.PAR_RANGES + "='" + logPrefix + "'");

            // add the current settings
            arguments.addAll(Arrays.asList(currentParameters));

            // print each combination
            System.out.println(String.join(" ", arguments));
        }
    }

    /**
     * Generate the list of arguments common to each simulation.
     *
     * @param args Command line arguments.
     * @return Common arguments.
     */
    private static List<String> commonArguments(String[] args) {

        // accumulate the arguments common to each run of the simulator
        final List<String> arguments = new ArrayList<>(4);

        // main for a single simulation
        arguments.add("peersim.Simulator");

        // command line parameters
        arguments.add(args[0]);

        // since multiple experiments are managed here, the value
        // of standard variable for multiple experiments is changed to 1
        arguments.add(Simulator.PAR_EXPS + "=1");

        // activate redirection to separate stdout from stderr
        arguments.add(Simulator.PAR_REDIRECT + "=" + TaggedOutputStream.class.getCanonicalName());

        return arguments;
    }
}
