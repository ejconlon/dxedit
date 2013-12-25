from .enums import *
from .matchers import *
from .constants import *

messages = {
    'dx_param_change': [
        (B.sysex_start, N.one, match_equals(start_tag)),
        (B.mfr_id, N.one, match_equals(yamaha_mfr_id)),
        (B.device_num, N.one, match_like('0001nnnn')),
        (B.parameter_group_num, N.one, match_seven()),
        (B.parameter_num, N.one, match_seven()),
        (B.data, N.one, match_seven()),
        (B.sysex_end, N.one, match_equals(end_tag))
    ],

    'native_bulk_dump': [
        (B.sysex_start, N.one, match_equals(start_tag)),
        (B.mfr_id, N.one, match_equals(yamaha_mfr_id)),
        (B.device_num, N.one, match_like('0000nnnn')),
        (B.model_id, N.one, match_equals(native_bulk_dump_model_id)),
        (B.byte_count_msb, N.one, match_seven()),
        (B.byte_count_lsb, N.one, match_seven()),
        (B.addr_high, N.one, match_seven()),
        (B.addr_mid, N.one, match_seven()),
        (B.addr_low, N.one, match_seven()),
        (B.data, N.many, match_seven()),
        (B.checksum, N.one, match_seven()),
        (B.sysex_end, N.one, match_equals(end_tag))
    ]
}
