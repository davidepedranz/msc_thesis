#!/usr/bin/env python3

#
# Copyright (c) 2018 Davide Pedranz. All rights reserved.
#
# This code is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program. If not, see <https://www.gnu.org/licenses/>.
#

import sys
import pandas
import utils
import parser
import plotter
import joblib


def main():
    """
    Compute various kinds of plots useful to analyze the results of one simulation.
    """

    # check the command line parameters
    argv = sys.argv
    if len(argv) != 2:
        print('Usage: %s <path_to_simulation>' % argv[0])
        exit(1)

    # read the simulation folder
    run_dir = argv[1]
    print('Processing simulation at: %s' % run_dir)

    # load the cache or parse the file
    stats, freq, parameters = load_cache_or_parse_logs(run_dir)

    # create directory for the plots ...
    plots_dir = run_dir + 'plots'
    utils.mkdir(plots_dir)

    # [plots]: extract simulations parameters
    params_no_seed = filter_seed(parameters)
    params_line_charts = params_no_seed

    # [plots]: forks rate
    print(' - Process Forks Rate')
    with joblib.Parallel(n_jobs=joblib.cpu_count()) as parallel:
        parallel(
            joblib.delayed(plotter.forks_rate_line_chart)
            (stats=stats, freq=freq, agg_diff=filter_list(params_line_charts, param), agg_same=param, out_dir=plots_dir)
            for param in params_line_charts
        )

    # [plots]: forks number
    print(' - Process Forks Number')
    with joblib.Parallel(n_jobs=joblib.cpu_count()) as parallel:
        parallel(
            joblib.delayed(plotter.forks_number_line_chart)
            (freq=freq, agg_diff=filter_list(params_line_charts, param), agg_same=param, out_dir=plots_dir)
            for param in params_line_charts
        )

    # [plots]: messages
    print(' - Process Protocols Metrics')
    with joblib.Parallel(n_jobs=joblib.cpu_count()) as parallel:
        parallel(
            joblib.delayed(plotter.messages_line_chart)
            (stats=stats, agg_diff=filter_list(params_line_charts, param), agg_same=param, out_dir=plots_dir)
            for param in params_line_charts
        )

    # [plots]: filter out unwanted parameters (for now...)
    # params_no_seed_with_time = params_no_seed + ['time']
    # params_distributions = params_no_seed_with_time
    # [plots]: forks distribution
    # print(' - Process Forks Distribution')
    # with joblib.Parallel(n_jobs=joblib.cpu_count()) as parallel:
    #     parallel(
    #         joblib.delayed(plotter.forks_distribution_histogram)
    #         (freq=freq, agg_diff=filter_list(params_distributions, param), agg_same=param, out_dir=plots_dir)
    #         for param in params_distributions
    #     )
    #     parallel(
    #         joblib.delayed(plotter.forks_distribution_line_chart)
    #         (freq=freq, agg_diff=filter_list(params_distributions, param), agg_same=param, out_dir=plots_dir)
    #         for param in params_distributions
    #     )


def load_cache_or_parse_logs(run_dir):
    """
    Loads the results of a given simulation either from the cache on disk,
    if found, or directly parsing the simulation log.
    :param run_dir: Directory that contains the simulation logs and configuration.
    :return: Simulation results.
    """

    # compute paths of relevant files
    log_file = run_dir + 'stdout.txt'
    cache_dir = run_dir + 'cache/'
    stats_file = cache_dir + 'stats.csv'
    freq_file = cache_dir + 'freq.csv'
    params_file = cache_dir + 'params.csv'

    # cache found, load it
    if utils.exits(stats_file) and utils.exits(freq_file) and utils.exits(params_file):
        print(' - Cache found... do NOT process the results again')
        stats = pandas.read_csv(stats_file)
        freq = pandas.read_csv(freq_file)
        parameters = list(pandas.read_csv(params_file)['parameters'])

    # cache not found
    else:
        print(' - Cache NOT found... process the results')
        stats, freq, parameters = parser.parse_file(log_file)
        utils.mkdir(cache_dir)
        stats.to_csv(stats_file, index=False)
        freq.to_csv(freq_file, index=False)
        pandas.DataFrame({'parameters': parameters}).to_csv(params_file, index=False)

    # return the parsed logs, either taken from the cache or computed from the simulation log
    return stats, freq, parameters


def filter_seed(_list):
    """
    Remove the 'seed' string from the given list.
    :param _list: List of string.
    :return: Input list without any occurrence of 'seed'.
    """
    return filter_list(_list, 'seed')


def filter_list(_list, item):
    """
    Remove the given string from the given list.
    :param _list: List of string.
    :param item: String to remove.
    :return: Input list without any occurrence of the string to remove.
    """
    return [x for x in _list if x != item]


# script entry-point
if __name__ == '__main__':
    main()
