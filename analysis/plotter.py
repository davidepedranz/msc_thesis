import itertools
import utils
import numpy as np
import matplotlib as mpl

mpl.use('Agg')
import matplotlib.pyplot as plt
import matplotlib.ticker as plticker


def messages_plot(data, protocol, agg_diff=('network_size', 'delay'), agg_same='delay', out_dir='plots'):
    """
    Generate a plot for each metric collected for a given given protocol.
    :param data: Pandas DataFrame that contains the simulator logs.
    :param protocol: PeerSim protocol that generated the messages.
    :param agg_diff: Columns to use for the aggregation.
    :param agg_same: Column to use for the aggregation, generating multiple data series for the same plot.
    :param out_dir: Path of the directory where to store the generated plots.
    """
    metrics = data.query('protocol == "%s"' % protocol)['metric'].unique()
    for metric in metrics:
        _dir = out_dir + '/' + str(metric)
        message_plot(data, protocol, metric, agg_diff=agg_diff, agg_same=agg_same, out_dir=_dir)


def message_plot(data, protocol, metric, agg_diff=('network_size', 'delay'), agg_same='delay', out_dir='plots'):
    """
    Generate a plot of the number of messages (metric) used by a given protocol.
    :param data: Pandas DataFrame that contains the simulator logs.
    :param protocol: PeerSim protocol that generated the messages.
    :param metric: Name of the PeerSim metric.
    :param agg_diff: Columns to use for the aggregation.
    :param agg_same: Column to use for the aggregation, generating multiple data series for the same plot.
    :param out_dir: Path of the directory where to store the generated plots.
    """

    # prepare the folders for the plots
    utils.mkdir(out_dir)

    # extract the logs for the given protocol and metric
    # aggregation_with_time = list(agg_diff) + ['time'] # TODO: remove
    aggregation_with_time = list(agg_diff) + ([agg_same] if agg_same is not None else []) + ['time']
    df = data \
        .query('protocol == "%s" and metric == "%s"' % (protocol, metric)) \
        .groupby(aggregation_with_time, as_index=False) \
        .agg({'mean': ['mean', 'std']})

    # rename the columns to reduce the multilevel index to a single level
    df.columns = ['%s_%s' % (cols[0], cols[1]) if cols[1] != '' else cols[0] for cols in df.columns]

    # extract the unique combinations of parameters to use for the plots
    params = df.drop_duplicates(subset=agg_diff)

    # make one plot for each combination of parameters
    for _, row in params.iterrows():
        query = make_query(agg_diff, row)
        match = df.query(query)

        print(' - Metric [protocol=%s, metric=%s]: %s' % (protocol, metric, query))

        figure = plt.figure()
        ax = figure.add_subplot(111)

        if agg_same is not None:
            style = itertools.cycle(["-", "--", "-.", ":"])
            traces = match[agg_same].unique()
            for trace in traces:
                current = match.query('%s == %f' % (agg_same, trace))
                label = "%s=%d" % (agg_same, trace)

                x = list(current['time'])
                y = list(current['mean_mean'])
                y_err = list(current['mean_std'])
                ax.errorbar(x, y, yerr=y_err, label=label, linestyle=style.__next__())
            ax.legend()

        else:
            x = list(match['time'])
            y = list(match['mean_mean'])
            y_err = list(match['mean_std'])
            ax.errorbar(x, y, yerr=y_err)

        ax.set_xlabel('Time (ms)')
        ax.set_ylabel('Number of messages')
        ax.set_title(metric + ': ' + make_title(agg_diff, row))
        ax.grid(True)

        path = '%s/messages-%s-%s-plot-%s.png' % (out_dir, protocol, metric, make_filename(agg_diff, row))
        figure.savefig(path, bbox_inches='tight')
        plt.close(figure)


def blockchain_histogram(data, agg_diff=('network_size', 'time'), agg_same='delay', out_dir='plots'):
    """
    Generate histograms of the frequencies of forks of different sizes (in the global blockchain).
    :param data: Pandas DataFrame that contains the simulator logs.
    :param agg_diff: Columns to use for the aggregation. Each unique combination of values
    of this columns will generate a different plot.
    :param agg_same: Column to use for the aggregation, generating multiple data series for the same plot.
    :param out_dir: Path of the directory where to store the generated plots.
    """

    # prepare the folders for the plots
    output_dir = out_dir + '/global-blockchain'
    utils.mkdir(output_dir)

    # extract the logs of the global blockchain
    agg = list(agg_diff) + ([agg_same] if agg_same is not None else []) + ['value']
    df = data \
        .query('protocol == "core" and metric == "global-blockchain"') \
        .groupby(agg, as_index=False) \
        .agg({'frequency': 'mean'})

    # extract the unique combinations of parameters to use for the plots (and exclude time == 0)
    params = df.query('time > 0').drop_duplicates(subset=agg_diff)

    # make one plot for each combination of parameters
    for _, row in params.iterrows():
        query = make_query(agg_diff, row)
        match = df.query(query)

        print(' - Global Blockchain: %s' % query)

        figure = plt.figure()
        ax = figure.add_subplot(111)

        if agg_same is not None:
            traces = match[agg_same].unique()
            for i, trace in enumerate(traces):
                current = match.query('%s == %f' % (agg_same, trace))
                values = current['value']
                frequencies = current['frequency']

                width = 0.8
                n = float(len(traces))
                x = np.asarray(list(values)) - (width / 2) + (i / n * width)
                label = "%s=%d" % (agg_same, trace)
                ax.bar(x, frequencies, width=0.8 / n, label=label, align='edge')
            ax.legend()

        else:
            values = match['value']
            frequencies = match['frequency']
            ax.bar(values, frequencies)

        ax.set_yscale("log")

        ax.set_xlabel('Fork Size')
        ax.set_ylabel('Frequency')
        ax.set_title('Global Forks: ' + make_title(agg_diff, row))
        ax.grid(True)
        ax.xaxis.set_major_locator(mpl.ticker.MultipleLocator(base=1.0))

        path = '%s/%s.png' % (output_dir, make_filename(agg_diff, row))
        figure.savefig(path, bbox_inches='tight')
        plt.close(figure)


def make_query(columns, row):
    return make_stuff(columns, row, sep_1=' and ', sep_2=' == ')


def make_title(columns, row):
    return make_stuff(columns, row, sep_1=', ', sep_2='=')


def make_filename(columns, row):
    return make_stuff(columns, row, sep_1='-', sep_2='-')


def make_stuff(columns, row, sep_1, sep_2):
    return sep_1.join([column + sep_2 + str(float(row[column])) for column in columns])
