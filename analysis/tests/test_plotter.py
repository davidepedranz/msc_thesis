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
