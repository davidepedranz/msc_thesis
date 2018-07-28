#!/usr/bin/env python3

import sys
import pandas
import utils
import parser
import plotter


def main():
    """"""

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

    # create directory for the plots ... TODO (uncomment line)
    # plots_dir = run_dir + 'plots'
    plots_dir = 'plots'
    utils.mkdir(plots_dir)

    # [plots]: extract simulations parameters
    params_no_seed = filter_seed(parameters)
    params_no_seed_with_time = params_no_seed + ['time']

    # [plots]: messages
    plotter.messages_plot(stats, protocol='topology', agg_diff=params_no_seed, agg_same=None, out_dir=plots_dir)
    plotter.messages_plot(stats, protocol='topology', agg_diff=('network_size',), agg_same='delay', out_dir=plots_dir)

    # [plots]: blockchain
    plotter.blockchain_histogram(freq, agg_diff=params_no_seed_with_time, agg_same=None, out_dir=plots_dir)
    plotter.blockchain_histogram(freq, agg_diff=('network_size', 'time'), agg_same='delay', out_dir=plots_dir)
    plotter.blockchain_histogram(freq, agg_diff=('delay', 'time'), agg_same='network_size', out_dir=plots_dir)


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
        print('Cache found for simulation: ' + log_file)
        stats = pandas.read_csv(stats_file)
        freq = pandas.read_csv(freq_file)
        parameters = list(pandas.read_csv(params_file)['parameters'])

    # cache not found
    else:
        print('Cache NOT found for simulation: ' + log_file)
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
    return [x for x in _list if x != 'seed']


# script entry-point
if __name__ == '__main__':
    main()
