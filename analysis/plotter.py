import itertools
import utils
import numpy as np
import matplotlib as mpl

mpl.use('Agg')
import matplotlib.pyplot as plt
import matplotlib.ticker as plticker


def messages_line_chart(data, agg_diff=('network_size', 'delay'), agg_same='delay', out_dir='plots'):
    """
    Generate a plot for each metric collected for a given given protocol.
    :param data: Pandas DataFrame that contains the simulator logs.
    :param agg_diff: Columns to use for the aggregation.
    :param agg_same: Column to use for the aggregation, generating multiple data series for the same plot.
    :param out_dir: Path of the directory where to store the generated plots.
    """
    protocols = data['protocol'].unique()
    for protocol in protocols:
        metrics = data.query('protocol == "%s"' % protocol)['metric'].unique()
        for metric in metrics:
            message_line_chart(data, protocol, metric, agg_diff=agg_diff, agg_same=agg_same, out_dir=out_dir)


def message_line_chart(data, protocol, metric, agg_diff, agg_same, out_dir):
    """
    Generate a plot of the number of messages (metric) used by a given protocol.
    :param data: Pandas DataFrame that contains the simulator logs.
    :param protocol: PeerSim protocol that generated the messages.
    :param metric: Name of the PeerSim metric.
    :param agg_diff: Columns to use for the aggregation.
    :param agg_same: Column to use for the aggregation, generating multiple data series for the same plot.
    :param out_dir: Path of the directory where to store the generated plots.
    """

    # TODO: remove str HACK!
    # prepare the folders for the plots
    output_dir = '/'.join([out_dir, protocol, str(metric)])
    utils.mkdir(output_dir)

    # extract the logs for the given protocol and metric
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

        print('   - Metric: protocol == %s, metric == %s, %s' % (protocol, metric, query))

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
                ax.errorbar(x, y, yerr=y_err, label=label, linestyle=style.__next__(), capsize=5)
            ax.legend()

        else:
            x = list(match['time'])
            y = list(match['mean_mean'])
            y_err = list(match['mean_std'])
            ax.errorbar(x, y, yerr=y_err, capsize=5)

        ax.set_xlabel('Time (ms)')
        ax.set_ylabel('Number of messages')
        ax.set_title(metric + ': ' + make_title(agg_diff, row))
        ax.grid(True)

        path = '%s/%s.png' % (output_dir, make_filename(agg_diff, row))
        figure.savefig(path, bbox_inches='tight')
        plt.close(figure)


def forks_histogram(data, agg_diff=('network_size', 'time'), agg_same='delay', out_dir='plots'):
    """
    Generate histograms of the frequencies of forks of different sizes (in the global blockchain).
    :param data: Pandas DataFrame that contains the simulator logs.
    :param agg_diff: Columns to use for the aggregation. Each unique combination of values
    of this columns will generate a different plot.
    :param agg_same: Column to use for the aggregation, generating multiple data series for the same plot.
    :param out_dir: Path of the directory where to store the generated plots.
    """

    # prepare the folders for the plots
    output_dir = out_dir + '/blockchain-forks'
    utils.mkdir(output_dir)

    # extract the logs of the global blockchain
    agg = list(agg_diff) + ([agg_same] if agg_same is not None else []) + ['value']
    df = data \
        .query('protocol == "core" and metric == "global-blockchain" and value > 0') \
        .groupby(agg, as_index=False) \
        .agg({'frequency': 'mean'})

    # extract the unique combinations of parameters to use for the plots (and exclude time == 0)
    params = df.query('time > 0').drop_duplicates(subset=agg_diff)

    # make one plot for each combination of parameters
    for _, row in params.iterrows():
        query = make_query(agg_diff, row)
        match = df.query(query)

        print('   - Blockchain Forks [histogram]: %s' % query)

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
        ax.set_title('Blockchain Forks: ' + make_title(agg_diff, row))
        ax.grid(True)
        ax.xaxis.set_major_locator(mpl.ticker.MultipleLocator(base=1.0))

        path = '%s/%s-histogram.png' % (output_dir, make_filename(agg_diff, row))
        figure.savefig(path, bbox_inches='tight')
        plt.close(figure)


def forks_line_chart(data, agg_diff=('network_size', 'time'), agg_same='delay', log_scale=True, out_dir='plots'):
    """
    Generate a line chart of the frequencies of forks of different sizes (in the global blockchain).Î
    :param data: Pandas DataFrame that contains the simulator logs.
    :param agg_diff: Columns to use for the aggregation. Each unique combination of values
    of this columns will generate a different plot.
    :param agg_same: Column to use for the aggregation, generating multiple data series for the same plot.
    :param log_scale: Use log scale for the y axis.
    :param out_dir: Path of the directory where to store the generated plots.
    """

    # prepare the folders for the plots
    output_dir = out_dir + '/blockchain-forks'
    utils.mkdir(output_dir)

    # extract the logs of the global blockchain
    agg = list(agg_diff) + ([agg_same] if agg_same is not None else []) + ['value']
    df = data \
        .query('protocol == "core" and metric == "global-blockchain" and value > 0 and time > 0') \
        .groupby(agg, as_index=False) \
        .agg({'frequency': ['mean', 'std']})

    # rename the columns to remove the second-level index
    df.columns = [col[0] + ('_' if col[1] != '' else '') + col[1] for col in df.columns]

    # extract the unique combinations of parameters to use for the plots
    params = df.drop_duplicates(subset=agg_diff)

    # make one plot for each combination of parameters
    for _, row in params.iterrows():
        query = make_query(agg_diff, row)
        match = df.query(query)

        print('   - Blockchain Forks [line-chart]: %s' % query)

        figure = plt.figure()
        ax = figure.add_subplot(111)

        if agg_same is not None:
            style = itertools.cycle(["-", "--", "-.", ":"])
            all_xs = list(match['value'].unique())
            traces = match[agg_same].unique()
            for trace in traces:
                current = match.query('%s == %f' % (agg_same, trace))
                label = "%s=%d" % (agg_same, trace)

                xs = list(current['value'])
                ys = list(current['frequency_mean'])
                ys_err = list(current['frequency_std'])

                if log_scale:
                    xs_to_plot = xs
                    ys_to_plot = ys
                    ys_err_to_plot = ys_err
                else:
                    xs_to_plot = all_xs
                    ys_to_plot = complete_with_default_values(xs, ys, all_xs)
                    ys_err_to_plot = complete_with_default_values(xs, ys_err, all_xs)

                ax.errorbar(xs_to_plot, ys_to_plot, ys_err_to_plot, label=label, linestyle=style.__next__(),
                            capsize=5, marker='.')

            ax.legend()

        else:
            xs = list(match['value'])
            ys = list(match['frequency_mean'])
            ys_err = list(match['frequency_std'])
            ax.errorbar(xs, ys, ys_err, capsize=5)

        if log_scale:
            ax.set_yscale("log")

        ax.set_xlabel('Fork Size')
        ax.set_ylabel('Frequency')
        ax.set_title('Blockchain Forks: ' + make_title(agg_diff, row))
        ax.grid(True)
        ax.xaxis.set_major_locator(mpl.ticker.MultipleLocator(base=1.0))

        path = '%s/%s-line-chart.png' % (output_dir, make_filename(agg_diff, row))
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


def complete_with_default_values(xs, ys, all_xs, default_y=0):
    return list(map(lambda x: get_or_default(x, xs, ys, default_y), all_xs))


def get_or_default(x, xs, ys, default_y):
    try:
        index = xs.index(x)
        return ys[index]
    except ValueError:
        return default_y
