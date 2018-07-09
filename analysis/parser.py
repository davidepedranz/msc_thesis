from collections import namedtuple
import pandas as pd

common_attributes = [
    'protocol',
    'metric',
    'parameters',
    'time'
]

stats_attributes = common_attributes + [
    'min',
    'max',
    'n',
    'mean',
    'variance',
    'count_min',
    'count_max'
]

freq_attributes = common_attributes + ['value', 'frequency']

StatsLine = namedtuple('StatsLine', stats_attributes)

FreqLine = namedtuple('FreqLine', freq_attributes)


def parse_parameters(s):
    parameters = {}
    tokens = s.split(' ')
    assert len(tokens) % 2 == 0
    for i in range(len(tokens) // 2):
        parameters[tokens[2 * i].lower()] = float(tokens[(2 * i) + 1])
    return parameters


def parse_line(line):
    line_parts = line.split(': ', 1)
    protocol_parts = line_parts[0].split('-', 1)
    data_parts = line_parts[1].split(' [')
    measure_parts = data_parts[1].split('] ', 1)

    protocol = protocol_parts[0].replace('control.', '')
    metric = protocol_parts[1] if len(protocol_parts) > 1 else ''
    parameters = parse_parameters(data_parts[0])
    time = measure_parts[0]
    raw_measure = measure_parts[1].strip()

    return protocol, metric, parameters, int(time), raw_measure


def is_stats_line(line):
    first_item = line.split('] ', 1)[1].split(' ')[0]
    try:
        float(first_item)
        return True
    except ValueError:
        return False


def parse_stats_line(line):
    protocol, metric, parameters, time, raw_measure = parse_line(line)
    measure = raw_measure.split(' ')
    return StatsLine(
        protocol=protocol,
        metric=metric,
        parameters=parameters,
        time=time,
        min=float(measure[0]),
        max=float(measure[1]),
        n=int(measure[2]),
        mean=float(measure[3]),
        variance=float(measure[4]),
        count_min=int(measure[5]),
        count_max=int(measure[6])
    )


def is_freq_line(line):
    return line.split('] ', 1)[1].strip()[0] == '('


def parse_tuple(t):
    tokens = t.replace('(', '').replace(')', '').split(',')
    return int(tokens[0]), int(tokens[1])


def parse_freq_line(line):
    protocol, metric, parameters, time, raw_measure = parse_line(line)
    items = [parse_tuple(t) for t in raw_measure.split(' ')]
    return [
        FreqLine(
            protocol=protocol,
            metric=metric,
            parameters=parameters,
            time=time,
            value=item[0],
            frequency=item[1]
        )
        for item in items
    ]


def parse_file(location):
    stats_lines = []
    freq_lines = []
    with open(location, 'r') as f:
        for line in f:
            if line == '\n':
                pass
            elif is_stats_line(line):
                stats = parse_stats_line(line)
                stats_lines.append(stats)
            elif is_freq_line(line):
                freq = parse_freq_line(line)
                freq_lines += freq
            else:
                assert False, 'Got unknown line "' + line.replace('\n', '') + '"'
    raw_stats = pd.DataFrame(stats_lines, columns=stats_attributes)
    raw_freq = pd.DataFrame(freq_lines, columns=freq_attributes)
    stats, stats_params = expand_df_dictionary(raw_stats)
    freq, freq_params = expand_df_dictionary(raw_freq)
    assert stats_params == freq_params
    return stats, freq, stats_params


def expand_df_dictionary(df, name='parameters'):
    parameters = df[name].apply(pd.Series)
    return pd.concat([parameters, df.drop([name], axis=1)], axis=1), parameters.columns.get_values().tolist()
