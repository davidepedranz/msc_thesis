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

from all import load_cache_or_parse_logs, filter_seed
from utils import mkdir
import itertools
import numpy as np
import matplotlib as mpl

mpl.use('Agg')
import matplotlib.pyplot as plt
import matplotlib.ticker as plticker

# conversion factor to go from minutes to milliseconds
MINUTES_TO_MILLIS = 1000 * 60

# distance between 2 ticks on the x axis
X_TICK_STEP = 10

LOGS_BASE_DIR = '../logs/'
PLOTS_DESTINATION_DIR = '../latex/plots/'
EXTENSION = 'pdf'

FIGURE_SIZE = (9.5, 5.2)
FIGURE_SIZE_SMALL = (9.5, 4.8)
FIGURE_SIZE_BIG = (9.5, 7)

LEGEND_SHADOW = False
LEGEND_BORDER_PAD = 0.5
LEGEND_FONT_SIZE = 10
LEGEND_ALPHA = 1
LEGEND_HANDLE_LEN = 3

TITLE_FONT_SIZE = 13
TITLE_Y_OFFSET = 1.02

LABEL_FONT_SIZE = 10.5
LABEL_PAD = 10

TICK_LABEL_SIZE = 10


def main():
    np.random.seed(1)
    mkdir(PLOTS_DESTINATION_DIR)

    # change default colors to a darker version, better for printing
    mpl.rcParams['axes.prop_cycle'] = plt.cycler(color=plt.cm.get_cmap('tab10').colors)

    hashrates()
    blocks_at_rest()
    forks_with_delay()
    forks_balance_delay_small()
    forks_balance_delay_big()
    forks_balance_delay_size()
    forks_balance_drop()
    forks_balance_partitions()


# noinspection SpellCheckingInspection
def hashrates():
    """"""

    labels = list(reversed([
        'BTC.com',
        'AntPool',
        'ViaBTC',
        'BTC.TOP',
        'SlushPool',
        'F2Pool',
        'Poolin',
        'DPOOL',
        'BitClub',
        'Bixin',
        'BWPool',
        'BitFury',
        'WAYI.CN',
        'BTCC',
        'Bitcoin.com',
        'Others'
    ]))

    values = list(reversed([
        20.2,
        13.8,
        11.9,
        11.6,
        11.6,
        8.0,
        4.8,
        2.6,
        2.5,
        2.3,
        1.9,
        1.7,
        1.6,
        1.5,
        1.0,
        3.0
    ]))
    assert (abs(100 - sum(values)) <= 0.0000001)

    # compute the custom colors
    v = np.array(values)
    colormap = plt.cm.get_cmap('Greens')
    colors = list(colormap((v - v.min() + 5) / (v.max() - v.min())))

    # create the figure
    figure, ax = plt.subplots(figsize=FIGURE_SIZE_BIG)

    # plot the data
    ax.barh(labels, values, color=colors)
    ax.set_title('Hashrate distribution of the largest mining pools', fontsize=TITLE_FONT_SIZE, y=TITLE_Y_OFFSET)
    ax.set_xlabel('Hashrate ratio (%)', fontsize=LABEL_FONT_SIZE, labelpad=LABEL_PAD)
    ax.set_ylabel('Mining Pool', fontsize=LABEL_FONT_SIZE, labelpad=LABEL_PAD)
    ax.xaxis.set_major_locator(plticker.MultipleLocator(base=2))

    # save the figure
    path = '%s%s.%s' % (PLOTS_DESTINATION_DIR, 'hashrates', EXTENSION)
    figure.savefig(path, bbox_inches='tight')
    plt.close(figure)


def blocks_at_rest():
    """"""

    # load the correct simulation
    simulation = 'run-029-rest-1000-5000-9000/'
    stats, _, params = load_cache_or_parse_logs(LOGS_BASE_DIR + simulation)

    # compute the blocks over time
    agg = aggregation_params_line_chart(params)
    blocks = stats \
        .query('protocol == "core" and metric == "blocks"') \
        .groupby(agg, as_index=False) \
        .agg({'mean': 'mean'}) \
        .rename(columns={'mean': 'value'})

    # generate the plot
    title = 'Blocks generation over time'
    line_chart(
        df=blocks,
        agg_same='network_size',
        title=title,
        y_label='Blocks',
        y_step=2,
        file='blocks_rest_linechart',
        size=FIGURE_SIZE_SMALL
    )


def forks_with_delay():
    """"""

    simulations = ['run-022-delays-size-1000/', 'run-031-delays-size-9000/']
    sizes = [1000, 9000]

    for (simulation, size) in zip(simulations, sizes):
        forks_aggregated, forks_end_time = compute_forks(simulation)

        # generate the line chart
        line_chart(
            df=forks_aggregated,
            agg_same='delay',
            title='Forks number for a network of %s nodes with different delays' % size,
            y_label='Average forks number',
            y_lim=(-0.06, 1.18),
            y_step=0.1,
            legend_formatter=lambda x: 'delay = {:g} s'.format(x / 1000),
            file='forks_delay_%s_linechart' % size
        )

        # generate the box plot
        nice_box_plot(
            df=forks_end_time,
            agg_same='delay',
            titles=['Forks distribution for a network of %s nodes with different delays' % size],
            x_label='Delay (s)',
            y_label='Forks',
            x_spread=0.5,
            y_spread=0.5,
            x_scaler=lambda x: int(x / 1000),
            size=8,
            alpha=0.7,
            file='forks_delay_%s_boxplot' % size
        )


def forks_balance_delay_small():
    """"""

    size = 1000
    simulation_rest = 'run-024-attack-delay-0-to-2-size-1000-2000/'
    query_rest = 'balance_attack_delay_minutes == 0 and network_size == %s' % size
    forks_aggregated_rest, forks_end_time_rest = compute_forks(simulation_rest, query=query_rest)
    simulation_delays = 'run-034-attack-delays-size-1000/'
    forks_aggregated_delays, forks_end_time_delays = compute_forks(simulation_delays, query='seed != 3208')
    forks_aggregated = forks_aggregated_rest.append(forks_aggregated_delays)
    forks_end_time = forks_end_time_rest.append(forks_end_time_delays)

    # generate the line chart
    line_chart(
        df=forks_aggregated,
        agg_same='balance_attack_delay_minutes',
        title='Forks number for a network of %s nodes under Balance attack' % size,
        y_label='Average forks number',
        y_step=0.2,
        legend_formatter=lambda x: 'delay = {:g} s'.format(round(x * 60)),
        file='forks_attack_delay_%s_linechart' % size
    )

    # generate the box plot
    nice_box_plot(
        df=forks_end_time,
        agg_same='balance_attack_delay_minutes',
        titles=[
            'Forks distribution for a network of %s nodes under Balance attack' % size,
            'Forks distribution (network size = %s)' % size
        ],
        x_label='Delay (s)',
        y_label='Forks',
        x_spread=0.5,
        y_spread=0.5,
        x_scaler=lambda x: int(round(x * 60)),
        size=8,
        alpha=0.7,
        file='forks_attack_delay_%s_boxplot' % size
    )


def forks_balance_delay_big():
    """"""

    simulations = ['run-024-attack-delay-0-to-2-size-1000-2000/']
    sizes = [1000]

    for (simulation, size) in zip(simulations, sizes):
        query = 'seed != 3208 and network_size == %s' % size
        forks_aggregated, forks_end_time = compute_forks(simulation, query=query)

        # generate the line chart
        line_chart(
            df=forks_aggregated,
            agg_same='balance_attack_delay_minutes',
            title='Forks number for a network of %s nodes under Balance attack' % size,
            y_label='Average forks number',
            y_step=0.5,
            legend_formatter=lambda x: 'delay = {:g} s'.format(round(x * 60)),
            file='forks_attack_delay_big_%s_linechart' % size
        )

        # generate the box plot
        nice_box_plot(
            df=forks_end_time,
            agg_same='balance_attack_delay_minutes',
            titles=['Forks distribution for a network of %s nodes under Balance attack' % size],
            x_label='Delay (s)',
            y_label='Forks',
            x_spread=0.5,
            y_spread=0.5,
            x_scaler=lambda x: int(round(x * 60)),
            size=8,
            alpha=0.7,
            file='forks_attack_delay_big_%s_boxplot' % size
        )


def forks_balance_delay_size():
    """"""

    # c --> + ' and time == 10800000'

    for delay in [0.25, 0.5]:
        query = 'balance_attack_delay_minutes == %s' % delay
        _, forks_end_time_a = compute_forks('run-035/', query=query)
        _, forks_end_time_b = compute_forks('run-036/', query=query)
        _, forks_end_time_c = compute_forks('run-033-attack-all-sizes-delay-30-60-s/', query=query)
        _, forks_end_time_d = compute_forks('run-046/', query=query)
        forks_end_time = forks_end_time_a \
            .append(forks_end_time_b) \
            .append(forks_end_time_c) \
            .append(forks_end_time_d)

        # generate the box plot
        delay_seconds = int(delay * 60)
        nice_box_plot(
            df=forks_end_time,
            agg_same='network_size',
            titles=[
                'Forks distribution for different networks under Balance attack (delay = %s s)' % delay_seconds,
                'Forks distribution (delay = %s s)' % delay_seconds
            ],
            x_scaler=lambda x: int(x),
            x_label='Network Size',
            y_label='Forks',
            x_spread=0.5,
            y_spread=0.3,
            size=15,
            alpha=0.7,
            file='forks_attack_delay_%s_network_sizes_boxplot' % delay_seconds
        )


def forks_balance_drop():
    """"""

    size = 1000
    simulation = 'run-026-attack-drop-1000/'
    forks_aggregated, forks_end_time = compute_forks(simulation, query='balance_attack_drop >= 0.5')

    # generate the line chart
    line_chart(
        df=forks_aggregated,
        agg_same='balance_attack_drop',
        title='Forks number for a network of %s nodes under Balance attack with messages drop' % size,
        y_label='Average forks number',
        y_step=2,
        legend_formatter=lambda x: 'drop = {:g}'.format(x),
        file='forks_attack_drop_linechart'
    )

    # generate the box plot
    nice_box_plot(
        df=forks_end_time,
        agg_same='balance_attack_drop',
        titles=[
            'Forks distribution for a network of %s nodes under Balance attack with messages drop' % size,
            'Forks distribution (network size = %s, delay = 0 s)' % size
        ],
        x_label='Drop probability',
        y_label='Forks',
        x_spread=0.5,
        y_spread=0.5,
        size=8,
        alpha=0.7,
        file='forks_attack_drop_boxplot'
    )


def forks_balance_partitions():
    """"""

    simulation = 'run-038/'
    _, forks_end_time = compute_forks(simulation, query='seed != 5187')

    # generate the box plot
    nice_box_plot(
        df=forks_end_time,
        agg_same='balance_attack_partitions',
        titles=[
            'Forks distribution for different networks under Balance attack for different partitions',
            'Forks distribution (network size = 1000, delay = 30 s)'
        ],
        x_scaler=lambda x: int(x),
        x_label='Number of partitions',
        y_label='Forks',
        x_spread=0.5,
        y_spread=0.35,
        size=8,
        alpha=0.7,
        file='forks_attack_partitions_boxplot'
    )


def compute_forks(simulation, query=None):
    """"""

    # load the correct simulation
    _, freq, params = load_cache_or_parse_logs(LOGS_BASE_DIR + simulation)

    # apply any given filters
    if query is not None:
        freq = freq.query(query)

    # compute the forks
    agg = aggregation_params_line_chart(params)
    forks_all = freq \
        .query('protocol == "core" and metric == "global-blockchain"') \
        .groupby(['seed'] + agg, as_index=False) \
        .agg({'frequency': 'sum'}) \
        .rename(columns={'frequency': 'value'})
    forks_all['value'] = forks_all['value'] - 1
    forks_aggregated = forks_all \
        .groupby(agg, as_index=False) \
        .agg({'value': 'mean'})
    time_max = forks_all['time'].max() if len(forks_all['time']) > 0 else 0
    forks_end_time = forks_all \
        .query('seed != 1223') \
        .query('time == ' + str(time_max))

    return forks_aggregated, forks_end_time


def line_chart(df, agg_same, title, y_label, y_step, file, y_lim=None, legend_formatter=None, size=FIGURE_SIZE):
    """"""

    # create the figure
    figure = plt.figure(figsize=size)
    ax = figure.add_subplot(111)

    # plot the single traces
    values = df[agg_same].unique()
    lines = line_styles()
    for value in values:
        current = df.query('%s == %s' % (agg_same, value))
        x = list(current['time'] / MINUTES_TO_MILLIS)
        y = list(current['value'])
        if legend_formatter is None:
            label = format_legend(agg_same, value)
        else:
            label = legend_formatter(value)
        ax.plot(x, y, label=label, linestyle=lines.__next__())

    # title, axes, legend, etc
    ax.set_title(title, fontsize=TITLE_FONT_SIZE, y=TITLE_Y_OFFSET)
    ax.set_xlabel('Time (minutes)', fontsize=LABEL_FONT_SIZE, labelpad=LABEL_PAD)
    ax.set_ylabel(y_label, fontsize=LABEL_FONT_SIZE, labelpad=LABEL_PAD)
    ax.grid(True, linestyle='dashed')
    ax.legend(shadow=LEGEND_SHADOW, borderpad=LEGEND_BORDER_PAD, fontsize=LEGEND_FONT_SIZE, framealpha=LEGEND_ALPHA,
              handlelength=LEGEND_HANDLE_LEN)
    ax.xaxis.set_major_locator(plticker.MultipleLocator(base=X_TICK_STEP))
    ax.yaxis.set_major_locator(plticker.MultipleLocator(base=y_step))
    ax.tick_params(axis='both', which='major', labelsize=TICK_LABEL_SIZE)
    if y_lim is not None:
        ax.set_ylim(y_lim)

    # save the figure
    path = '%s%s.%s' % (PLOTS_DESTINATION_DIR, file, EXTENSION)
    figure.savefig(path, bbox_inches='tight')
    plt.close(figure)


def nice_box_plot(df, agg_same, titles, x_label, y_label, x_spread, y_spread, size, alpha, file, x_scaler=None,
                  y_scale=0.65):
    """"""

    delta = df['value'].max() - df['value'].min()

    # process the data
    grouped = df.groupby(agg_same)
    xs, ys, ys_real, labels = [], [], [], []
    for i, (label, group) in enumerate(grouped):
        # x axis
        n = len(group)
        x = i + 1 + (np.random.rand(n) * x_spread) - (x_spread / 2)
        xs.append(x)

        # y axis
        y_original = group['value'].values
        ys_real.append(y_original)
        y_shift = np.random.rand(y_original.shape[0]) * y_spread - (y_spread / 2)

        curve = (-y_original / delta * y_scale + 1)
        y_shift_scaled = y_shift * curve

        y_modified = y_original + y_shift_scaled
        ys.append(y_modified)

        # trace label
        labels.append(label if x_scaler is None else x_scaler(label))

    # create the figure
    figure = plt.figure(figsize=FIGURE_SIZE)
    ax = figure.add_subplot(111)

    # boxplot
    ax.boxplot(ys_real, labels=labels, showfliers=False, whis='range', medianprops={'color': 'black'})

    # plot the single points on top of the boxplot
    for x, y in zip(xs, ys):
        ax.scatter(x, y, marker='.', s=size, alpha=alpha)

    # axes, legend, etc
    ax.set_title(titles, fontsize=TITLE_FONT_SIZE, y=TITLE_Y_OFFSET)
    ax.set_xlabel(x_label, fontsize=LABEL_FONT_SIZE, labelpad=LABEL_PAD)
    ax.set_ylabel(y_label, fontsize=LABEL_FONT_SIZE, labelpad=LABEL_PAD)
    ax.grid(True, linestyle='dashed')

    # handle different titles
    for (i, titles) in enumerate(titles):
        ax.set_title(titles, fontsize=TITLE_FONT_SIZE, y=TITLE_Y_OFFSET)

        # save the figure
        suffix = '' if i == 0 else '_' + str(i)
        path = '%s%s%s.%s' % (PLOTS_DESTINATION_DIR, file, suffix, EXTENSION)
        figure.savefig(path, bbox_inches='tight')
    plt.close(figure)


def aggregation_params_line_chart(params):
    return filter_seed(params) + ['time']


def format_legend(parameter, value):
    return "{} = {:g}".format(parameter, value)


def line_styles():
    return itertools.cycle([
        (0, ()),
        (0, (5, 2, 1, 2)),
        (0, (8, 3)),
        (0, (1, 2)),
        (0, (5, 2)),
        (0, (1, 2, 1, 2, 4, 2)),
        (0, (2, 3, 7, 3)),
    ])


if __name__ == '__main__':
    main()
