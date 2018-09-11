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
import plotter


class TestPlotter(unittest.TestCase):

    def test_make_query(self):
        actual = plotter.make_query(['a', 'b'], {'a': 1, 'b': 10.0})
        expected = 'a == 1.0 and b == 10.0'
        self.assertEqual(actual, expected)

    def test_make_title(self):
        actual = plotter.make_title(['a', 'b'], {'a': 1, 'b': 10.0})
        expected = 'a=1.0, b=10.0'
        self.assertEqual(actual, expected)

    def test_make_filename(self):
        actual = plotter.make_filename(['a', 'b'], {'a': 1, 'b': 10.0})
        expected = 'a-1.0-b-10.0'
        self.assertEqual(actual, expected)
