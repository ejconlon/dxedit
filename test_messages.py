#!/usr/bin/env python3 -m unittest

import unittest
from dxedit.matchers import *
from dxedit.enums import *
from dxedit.constants import *
from dxedit.messages import *

class TestMessages(unittest.TestCase):

    def test_match(self):
        start_section = (B.sysex_start, N.one, match_equals(start_tag))
        expected = (B.sysex_start, [start_tag])
        self.assertEquals(expected, match(start_tag, start_section))
        self.assertEquals(None, match(end_tag, start_section))

    def test_parse_seq_with_spec(self):
        dx_param_change_seq = [0xF0, 0x43, 0x10, 0x19, 0x4D, 0x00, 0xF7]
        other_seq = [0xF0, 0x43, 0x10, 0x62, 0x00, 0x00, 0x0E, 0x00, 0xF7]
        expected = [
            (B.sysex_start, [0xF0]),
            (B.mfr_id, [0x43]),
            (B.device_num, [0x00]), # match out a leading 0001
            (B.parameter_group_num, [0x19]),
            (B.parameter_num, [0x4D]),
            (B.data, [0x00]),
            (B.sysex_end, [0xF7])
        ]
        spec = message_specs['dx_param_change']
        actual_success = parse_seq_with_spec(dx_param_change_seq, spec)
        self.assertEquals(expected, actual_success)
        actual_failure = parse_seq_with_spec(other_seq, spec)
        self.assertEquals(None, actual_failure)


