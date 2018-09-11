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

import unittest
import pandas
import parser

parameters = 'NETWORK_SIZE 100 SEED 2'
stats_measure = '48.0 198.0 100 124.5 544.02 2 4'
freq_measure = '(0,167) (1,100)'
stats = 'control.topology-messages-ping: NETWORK_SIZE 100 SEED 2 [1080000] 48.0 198.0 100 124.5 544.02 2 4'
stats_inf = 'control.core-transactions: NETWORK_SIZE 10 SEED 1 [0] Infinity -Infinity 0 NaN 0.0 0 0'
freq = 'control.core-blockchain: NETWORK_SIZE 100 SEED 2 [480000]  (0,167) (1,100)'


class TestParser(unittest.TestCase):

    def test_parse_parameters(self):
        actual = parser.parse_parameters(parameters)
        expected = {'network_size': 100.0, 'seed': 2.0}
        self.assertEqual(actual, expected)

    def test_parse_line_begin_stats(self):
        actual = parser.parse_line(stats)
        expected = ('topology', 'messages-ping', parser.parse_parameters(parameters), 1080000, stats_measure)
        self.assertEqual(actual, expected)

    def test_parse_line_begin_freq(self):
        actual = parser.parse_line(freq)
        expected = ('core', 'blockchain', parser.parse_parameters(parameters), 480000, freq_measure)
        self.assertEqual(actual, expected)

    def test_is_stats_measure_true(self):
        actual = parser.is_stats_line(stats)
        self.assertTrue(actual)

    def test_is_stats_measure_inf_true(self):
        actual = parser.is_stats_line(stats)
        self.assertTrue(actual)

    def test_is_stats_measure_false(self):
        actual = parser.is_stats_line(freq)
        self.assertFalse(actual)

    def test_parse_stats_line(self):
        actual = parser.parse_stats_line(stats)
        expected = parser.StatsLine(
            protocol='topology',
            metric='messages-ping',
            parameters=parser.parse_parameters(parameters),
            time=1080000,
            min=48.0,
            max=198.0,
            n=100,
            mean=124.5,
            variance=544.02,
            count_min=2,
            count_max=4
        )
        self.assertEqual(actual, expected)

    def test_is_freq_measure_true(self):
        actual = parser.is_freq_line(freq)
        self.assertTrue(actual)

    def test_is_freq_measure_false(self):
        actual = parser.is_freq_line(stats)
        self.assertFalse(actual)

    def test_parse_tuple(self):
        actual = parser.parse_tuple('(0,100)')
        expected = (0, 100)
        self.assertEqual(actual, expected)

    def test_parse_freq_line(self):
        actual = parser.parse_freq_line(freq)
        expected = [
            parser.FreqLine(
                protocol='core',
                metric='blockchain',
                parameters=parser.parse_parameters(parameters),
                time=480000,
                value=0,
                frequency=167
            ),
            parser.FreqLine(
                protocol='core',
                metric='blockchain',
                parameters=parser.parse_parameters(parameters),
                time=480000,
                value=1,
                frequency=100
            )
        ]
        self.assertEqual(actual, expected)

    def test_parse_file(self):
        actual = parser.parse_file('example_stdout.txt')
        expected_parameters = ['network_size', 'seed']
        stats_attributes = expected_parameters + [x for x in parser.stats_attributes if x != 'parameters']
        expected_stats = pandas.DataFrame(columns=stats_attributes, data=[
            {
                'protocol': 'core',
                'metric': 'transactions',
                'time': 0,
                'min': float('inf'),
                'max': float('-inf'),
                'n': 0,
                'mean': float("nan"),
                'variance': 0.0,
                'count_min': 0,
                'count_max': 0,
                'network_size': 10.0,
                'seed': 1.0
            },
            {
                'protocol': 'topology',
                'metric': 'messages-addr',
                'time': 1080000,
                'min': 3.0,
                'max': 17.0,
                'n': 100,
                'mean': 8.49,
                'variance': 11.2,
                'count_min': 4,
                'count_max': 3,
                'network_size': 100.0,
                'seed': 1.0
            }
        ])
        freq_attributes = expected_parameters + [x for x in parser.freq_attributes if x != 'parameters']
        expected_freq = pandas.DataFrame(columns=freq_attributes, data=[
            {
                'protocol': 'core',
                'metric': 'blockchain',
                'time': 0,
                'value': 0,
                'frequency': 10,
                'network_size': 10.0,
                'seed': 1.0
            },
            {
                'protocol': 'core',
                'metric': 'blockchain',
                'time': 720000,
                'value': 0,
                'frequency': 108,
                'network_size': 100.0,
                'seed': 1.0
            },
            {
                'protocol': 'core',
                'metric': 'blockchain',
                'time': 720000,
                'value': 1,
                'frequency': 100,
                'network_size': 100.0,
                'seed': 1.0
            }
        ])
        self.assertTrue(pandas.DataFrame.equals(actual[0], expected_stats))
        self.assertTrue(pandas.DataFrame.equals(actual[1], expected_freq))
        self.assertEquals(actual[2], expected_parameters)


if __name__ == '__main__':
    unittest.main()
