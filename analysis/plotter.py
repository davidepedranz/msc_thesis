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

import itertools
import utils
import numpy as np
import matplotlib as mpl

mpl.use('Agg')
import matplotlib.pyplot as plt
import matplotlib.ticker as plticker

# conversion factor to go from minutes to milliseconds
MINUTES_TO_MILLIS = 1000 * 60

# make plots bigger than default
FIGURE_SIZE = (12, 9)
FIGURE_DPI = 100

# distance between 2 ticks on the x axis
X_TICK_STEP = 10


def messages_line_chart(stats, agg_diff, agg_same, out_dir, _all=False):
    """
    Generate a plot for each metric collected for a given given protocol.
    :param stats: Pandas DataFrame that contains the simulator logs.
    :param agg_diff: Columns to use for the aggregation.
    :param agg_same: Column to use for the aggregation, generating multiple data series for the same plot.
    :param out_dir: Path of the directory where to store the generated plots.
    :param _all: Consider all combination of parameters, even if agg_same has only one possible value.
    """
    protocols = stats['protocol'].unique()
    for protocol in protocols:
        metrics = stats.query('protocol == "%s"' % protocol)['metric'].unique()
        for metric in metrics:
            message_line_chart(stats, protocol, metric, agg_diff=agg_diff, agg_same=agg_same, _all=_all,
                               out_dir=out_dir)


def message_line_chart(stats, protocol, metric, agg_diff, agg_same, out_dir, _all=False):
    """
    Generate a plot of the given metric of the given protocol.
    :param stats: Pandas DataFrame that contains the simulator logs.
    :param protocol: PeerSim protocol that generated the messages.
    :param metric: Name of the PeerSim metric.
    :param agg_diff: Columns to use for the aggregation.
    :param agg_same: Column to use for the aggregation, generating multiple data series for the same plot.
    :param out_dir: Path of the directory where to store the generated plots.
    :param _all: Consider all combination of parameters, even if agg_same has only one possible value.
    """

    # prepare the folders for the plots
    output_dir = '/'.join([out_dir, protocol, str(metric)])
    utils.mkdir(output_dir)

    # extract the logs for the given protocol and metric
    aggregation_with_time = list(agg_diff) + ([agg_same] if agg_same is not None else []) + ['time']
    df = stats \
        .query('protocol == "%s" and metric == "%s"' % (protocol, metric)) \
        .groupby(aggregation_with_time, as_index=False) \
        .agg({'mean': ['mean', 'std']})

    # rename the columns to reduce the multilevel index to a single level
    df.columns = ['%s_%s' % (cols[0], cols[1]) if cols[1] != '' else cols[0] for cols in df.columns]

    # make the actual line charts
    line_charts(df=df, protocol=protocol, metric=metric, agg_diff=agg_diff, agg_same=agg_same, _all=_all,
                output_dir=output_dir)


def forks_number_line_chart(freq, agg_diff, agg_same, out_dir, _all=False):
    """
    Generate a plot of the number of forks over time.
    :param freq: Pandas DataFrame that contains the simulator logs.
    :param agg_diff: Columns to use for the aggregation.
    :param agg_same: Column to use for the aggregation, generating multiple data series for the same plot.
    :param out_dir: Path of the directory where to store the generated plots.
    :param _all: Consider all combination of parameters, even if agg_same has only one possible value.
    """

    # prepare the folders for the plots
    output_dir = out_dir + '/core/forks'
    utils.mkdir(output_dir)

    # sum forks, ignoring the depth
    agg = list(agg_diff) + ([agg_same] if agg_same is not None else []) + ['time']
    df = freq \
        .query('protocol == "core" and metric == "global-blockchain"') \
        .groupby(['seed'] + agg, as_index=False) \
        .agg({'frequency': 'sum'}) \
        .groupby(agg, as_index=False) \
        .agg({'frequency': ['mean', 'std']})

    # rename the columns to reduce the multilevel index to a single level
    df.columns = ['%s_%s' % (cols[0], cols[1]) if cols[1] != '' else cols[0] for cols in df.columns]
    df = df.rename(columns={'frequency_mean': 'mean_mean', 'frequency_std': 'mean_std'})

    # make the actual line charts
    line_charts(df=df, protocol='core', metric='forks', agg_diff=agg_diff, agg_same=agg_same, _all=_all,
                output_dir=output_dir)


def forks_rate_line_chart(stats, freq, agg_diff, agg_same, out_dir, _all=False):
    """
    TODO!!!
    :param stats:
    :param freq: 
    :param agg_diff: Columns to use for the aggregation.
    :param agg_same: Column to use for the aggregation, generating multiple data series for the same plot.
    :param out_dir: Path of the directory where to store the generated plots.
    :param _all: Consider all combination of parameters, even if agg_same has only one possible value.
    """

    # prepare the folders for the plots
    output_dir = out_dir + '/core/forks-rate'
    utils.mkdir(output_dir)

    aggregation_with_time = list(agg_diff) + ([agg_same] if agg_same is not None else []) + ['time']

    # compute the blocks
    blocks = stats \
        .query('protocol == "core" and metric == "blocks"') \
        .groupby(aggregation_with_time, as_index=False) \
        .agg({'mean': 'mean'}) \
        .rename(columns={'mean': 'blocks'})

    # compute the forks
    forks = freq \
        .query('protocol == "core" and metric == "global-blockchain"') \
        .groupby(['seed'] + aggregation_with_time, as_index=False) \
        .agg({'frequency': 'sum'}) \
        .groupby(aggregation_with_time, as_index=False) \
        .agg({'frequency': 'mean'}) \
        .rename(columns={'frequency': 'forks'})

    # join the 2 metrics
    join = blocks.set_index(aggregation_with_time) \
        .join(forks.set_index(aggregation_with_time)) \
        .reset_index() \
        .query('time > 0')
    join['rate'] = join['forks'] / join['blocks']

    # make the actual line charts
    line_charts(df=join, protocol='core', metric='forks-rate', agg_diff=agg_diff, agg_same=agg_same, _all=_all,
                output_dir=output_dir, y_field='rate', y_range=(0, 1))


def line_charts(df, protocol, metric, agg_diff, agg_same, _all, output_dir, y_field='mean_mean', y_range=None):
    """
    Generate line charts of the given protocol and metric for all parameters combination.

    :param df: Pandas DataFrame cleaned and ready to plot.
    :param protocol: PeerSim protocol that generated the messages.
    :param metric: Name of the PeerSim metric.
    :param agg_diff: Columns to use for the aggregation.
    :param agg_same: Column to use for the aggregation, generating multiple data series for the same plot.
    :param _all: Consider all combination of parameters, even if agg_same has only one possible value.
    :param output_dir: Absolute path where to store the plots.
    :param y_field: Name of the DataFrame column to use for the y axis.
    :param y_range: Override the default range of values for the y axis.
    """
    y_err_field = 'mean_std'

    # generate only plots that show at least 2 different traces!!!
    if not _all and len(df[agg_same].unique()) <= 1:
        return

    # extract the unique combinations of parameters to use for the plots
    params = df.drop_duplicates(subset=agg_diff)

    # make one plot for each combination of parameters
    for _, row in params.iterrows():
        query = make_query(agg_diff, row)
        match = df.query(query)

        print('   - Metric: protocol == %s, metric == %s, %s' % (protocol, metric, query))

        figure = plt.figure(figsize=FIGURE_SIZE)
        ax = figure.add_subplot(111)

        if agg_same is not None:
            style = itertools.cycle(["-", "--", "-.", ":"])
            traces = match[agg_same].unique()
            for trace in traces:
                current = match.query('%s == %f' % (agg_same, trace))
                label = "{}={}".format(agg_same, trace)

                x = list(current['time'] / MINUTES_TO_MILLIS)
                y = list(current[y_field])
                y_err = list(current[y_err_field]) if y_err_field in df.columns else None
                ax.errorbar(x, y, yerr=y_err, label=label, linestyle=style.__next__(), capsize=5)
            ax.legend()

        else:
            x = list(match['time'] / MINUTES_TO_MILLIS)
            y = list(match[y_field])
            y_err = list(match[y_err_field]) if y_err_field in df.columns else None
            ax.errorbar(x, y, yerr=y_err, capsize=5)

        ax.set_xlabel('Time (minutes)')
        ax.set_ylabel('Metric value')
        ax.set_title(metric + ': ' + make_title(agg_diff, row))
        ax.grid(True)

        if y_range is not None:
            ax.set_ylim(y_range)

        __, end = ax.get_xlim()
        ax.xaxis.set_ticks(np.arange(0, end, X_TICK_STEP))

        path = '%s/%s__%s.png' % (output_dir, agg_same, make_filename(agg_diff, row))
        figure.savefig(path, bbox_inches='tight', dpi=FIGURE_DPI)
        plt.close(figure)


def forks_distribution_line_chart(freq, agg_diff, agg_same, out_dir, y_log_scale=False, max_time_only=True):
    """
    Generate a line chart of the frequencies of forks of different sizes (in the global blockchain).Î
    :param freq: Pandas DataFrame that contains the simulator logs.
    :param agg_diff: Columns to use for the aggregation. Each unique combination of values
    of this columns will generate a different plot.
    :param agg_same: Column to use for the aggregation, generating multiple data series for the same plot.
    :param out_dir: Path of the directory where to store the generated plots.
    :param y_log_scale: Use the log scale for the y axis.
    :param max_time_only: Plot only the last measured metric.
    """

    # prepare the folders for the plots
    output_dir = out_dir + '/core/forks/distribution'
    utils.mkdir(output_dir)

    # extract the logs of the global blockchain
    agg = list(agg_diff) + ([agg_same] if agg_same is not None else []) + ['value']
    df = freq \
        .query('protocol == "core" and metric == "global-blockchain" and value > 0 and time > 0') \
        .groupby(agg, as_index=False) \
        .agg({'frequency': ['mean', 'std']})

    # rename the columns to remove the second-level index
    df.columns = [col[0] + ('_' if col[1] != '' else '') + col[1] for col in df.columns]

    # get only the maximum time
    if max_time_only:
        df = df[df['time'] == df['time'].max()]

    # extract the unique combinations of parameters to use for the plots
    params = df.drop_duplicates(subset=agg_diff)

    # make one plot for each combination of parameters
    for _, row in params.iterrows():
        query = make_query(agg_diff, row)
        match = df.query(query)

        print('   - Blockchain Forks [line-chart]: %s' % query)

        figure = plt.figure(figsize=FIGURE_SIZE)
        ax = figure.add_subplot(111)

        if agg_same is not None:
            style = itertools.cycle(["-", "--", "-.", ":"])
            all_xs = list(match['value'].unique())
            traces = match[agg_same].unique()
            for trace in traces:
                current = match.query('%s == %f' % (agg_same, trace))
                label = "{}={}".format(agg_same, trace)

                xs = list(current['value'])
                ys = list(current['frequency_mean'])
                ys_err = list(current['frequency_std'])

                if y_log_scale:
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

        if y_log_scale:
            ax.set_yscale("log")

        ax.set_xlabel('Fork Size')
        ax.set_ylabel('Frequency')
        ax.set_title('Blockchain Forks: ' + make_title(agg_diff, row))
        ax.grid(True)
        ax.xaxis.set_major_locator(mpl.ticker.MultipleLocator(base=1.0))

        path = '%s/%s-line-chart.png' % (output_dir, make_filename(agg_diff, row))
        figure.savefig(path, bbox_inches='tight', dpi=FIGURE_DPI)
        plt.close(figure)


def forks_distribution_histogram(freq, agg_diff, agg_same, out_dir, y_log_scale=False, max_time_only=True):
    """
    Generate histograms of the frequencies of forks of different sizes (in the global blockchain).
    :param freq: Pandas DataFrame that contains the simulator logs.
    :param agg_diff: Columns to use for the aggregation. Each unique combination of values
    of this columns will generate a different plot.
    :param agg_same: Column to use for the aggregation, generating multiple data series for the same plot.
    :param out_dir: Path of the directory where to store the generated plots.
    :param y_log_scale: Use the log scale for the y axis.
    :param max_time_only: Plot only the last measured metric.
    """

    # prepare the folders for the plots
    output_dir = out_dir + '/core/forks/distribution'
    utils.mkdir(output_dir)

    # extract the logs of the global blockchain
    agg = list(agg_diff) + ([agg_same] if agg_same is not None else []) + ['value']
    df = freq \
        .query('protocol == "core" and metric == "global-blockchain" and value > 0') \
        .groupby(agg, as_index=False) \
        .agg({'frequency': 'mean'})

    # get only the maximum time
    if max_time_only:
        df = df[df['time'] == df['time'].max()]

    # extract the unique combinations of parameters to use for the plots (and exclude time == 0)
    params = df.query('time > 0').drop_duplicates(subset=agg_diff)

    # make one plot for each combination of parameters
    for _, row in params.iterrows():
        query = make_query(agg_diff, row)
        match = df.query(query)

        print('   - Blockchain Forks [histogram]: %s' % query)

        figure = plt.figure(figsize=FIGURE_SIZE)
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
                label = "{}={}".format(agg_same, trace)
                ax.bar(x, frequencies, width=0.8 / n, label=label, align='edge')
            ax.legend()

        else:
            values = match['value']
            frequencies = match['frequency']
            ax.bar(values, frequencies)

        if y_log_scale:
            ax.set_yscale("log")

        ax.set_xlabel('Fork Size')
        ax.set_ylabel('Frequency')
        ax.set_title('Blockchain Forks: ' + make_title(agg_diff, row))
        ax.grid(True)
        ax.xaxis.set_major_locator(mpl.ticker.MultipleLocator(base=1.0))

        path = '%s/%s-histogram.png' % (output_dir, make_filename(agg_diff, row))
        figure.savefig(path, bbox_inches='tight', dpi=FIGURE_DPI)
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
