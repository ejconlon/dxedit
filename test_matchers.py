#!/usr/bin/env python3 -m unittest

import unittest
from dxedit.matchers import *


class TestMatchers(unittest.TestCase):

    # assertMatch* helpers

    def assertMatchEquals(self, gold, test):
        self.assertEquals([gold], match_equals(gold)(test))

    def assertNotMatchEquals(self, gold, test):
        self.assertEquals(None, match_equals(gold)(test))

    def assertMatchAny(self, test):
        self.assertEquals([test], match_any()(test))

    def assertMatchLike(self, expected, gold, test):
        self.assertEquals([expected], match_like(gold)(test))

    def assertNotMatchLike(self, gold, test):
        self.assertEquals(None, match_like(gold)(test))

    def assertMatchSeven(self, test):
        self.assertEquals([test], match_seven()(test))

    def assertNotMatchSeven(self, test):
        self.assertEquals(None, match_seven()(test))

    # test cases

    def test_match_equals(self):
        self.assertMatchEquals(3, 3)
        self.assertNotMatchEquals(3, 4)

    def test_match_any(self):
        self.assertMatchAny(3)

    def test_match_like(self):
        self.assertMatchLike(0xF, '1111vvvv', 0xFF)
        self.assertNotMatchLike('1111vvvv', 0xF)
        self.assertMatchLike(0xF, '0000vvvv', 0xF)
        self.assertNotMatchLike('0000vvvv', 0xFF)
        self.assertMatchLike(0xF0, 'vvvv0000', 0xF0)
        self.assertNotMatchLike('vvvv0000', 0xFF)
        self.assertMatchLike(0xF0, 'vvvv1111', 0xFF)
        self.assertNotMatchLike('vvvv1111', 0xF0)

    def test_match_seven(self):
        self.assertMatchSeven(0x7F)
        self.assertNotMatchSeven(0xFF)

