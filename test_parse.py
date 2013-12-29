
import unittest
from dxedit.messages import *
from dxedit.util import split_bytes
from dxedit.lookups import *

class TestParse(unittest.TestCase):
    frame = [0xF0, 0x43, 0x00, 0x62, 0x00, 0x05, 0x21, 0x7F,
             0x00, 0x03, 0x00, 0x01, 0x0C, 0x32, 0x19, 0xF7]

    parsed = {
        "Modulator Select": 0x03,
        "Scene Control": 0x00,
        "Common Tempo": 0x8C,
        "Play Effect Swing": 0x32
    }

    anno_data = AnnoData(AnnoTable({Tag.PATTERN: 0x7F}, table_voice_common_2), parsed)

    def frame_to_anno_data(self, frame):
        pseq = parse_seq(frame)
        assert pseq is not None
        message = pseq_to_message(pseq)
        assert message is not None
        return message_to_anno_data(message)

    def anno_data_to_frame(self, anno_data):
        # Step 1: Construct data array from parsed
        data = parsed_to_data(anno_data.parsed, anno_data.anno_table.table)
        assert data is not None
        # Step 2: Construct count bytes from data
        count_bytes = split_bytes(len(data))
        # Step 3: Construct address and model id from tags (and table?)
        address = get_address(anno_data.anno_table)
        assert address is not None
        # Step 4: Construct checksum from various byte fields
        # TODO refactor
        checksum = ((0xFF ^ (sum(address[1:]) + sum(count_bytes) + sum(data))) + 1) & 0x7F
        # Step 5: Frame packet
        # TODO refactor
        frame = [0xF0, 0x43, 0x00, address[0]] + list(count_bytes) + list(address[1:]) + data + [checksum, 0xF7]
        return frame

    def test_parse(self):
        actual = self.frame_to_anno_data(self.frame)
        self.assertEqual(self.anno_data, actual)

    def test_unparse(self):
        actual = self.anno_data_to_frame(self.anno_data)
        self.assertEqual(self.frame, actual)

