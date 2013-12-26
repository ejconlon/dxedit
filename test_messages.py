#!/usr/bin/env python3 -m unittest

import unittest
from dxedit.matchers import *
from dxedit.enums import *
from dxedit.constants import *
from dxedit.messages import *

class TestMessages(unittest.TestCase):

    dx_param_change_seq = [0xF0, 0x43, 0x10, 0x19, 0x4D, 0x00, 0xF7]
    native_bulk_dump_seq = [0xF0, 0x43, 0x00, 0x62, 0x00, 0x05, 0x21, 0x7F,
                            0x00, 0x03, 0x00, 0x01, 0x0C, 0x32, 0x19, 0xF7]

    def test_match(self):
        start_section = (B.sysex_start, N.one, match_equals(start_tag))
        expected = (B.sysex_start, [start_tag])
        self.assertEqual(expected, match(start_tag, start_section))
        self.assertEqual(None, match(end_tag, start_section))

    def test_parse_seq_with_spec(self):
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
        actual_success = parse_seq_with_spec(self.dx_param_change_seq, spec)
        self.assertEqual(expected, actual_success)
        actual_failure = parse_seq_with_spec(self.native_bulk_dump_seq, spec)
        self.assertEqual(None, actual_failure)

    def test_parse_many(self):
        expected = [
            (B.sysex_start, [0xF0]),
            (B.mfr_id, [0x43]),
            (B.device_num, [0x00]),
            (B.model_id, [0x62]),
            (B.byte_count_msb, [0x00]),
            (B.byte_count_lsb, [0x05]),
            (B.addr_high, [0x21]),
            (B.addr_mid, [0x7F]),
            (B.addr_low, [0x00]),
            (B.data, [0x03, 0x00, 0x01, 0x0C, 0x32]),
            (B.checksum, [0x19]),
            (B.sysex_end, [0xF7])
        ]
        spec = message_specs['native_bulk_dump']
        actual_success = parse_seq_with_spec(self.native_bulk_dump_seq, spec)
        self.assertEqual(expected, actual_success)
        actual_failure = parse_seq_with_spec(self.dx_param_change_seq, spec)
        self.assertEqual(None, actual_failure)


