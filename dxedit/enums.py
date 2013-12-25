from .enum import Enum

class B(Enum):
    sysex_start = 1
    sysex_end = 2
    mfr_id = 3
    device_num = 4
    model_id = 5
    checksum = 6
    addr_high = 7
    addr_mid = 8
    addr_low = 9
    data = 10
    parameter_group_num = 11
    parameter_num = 12
    byte_count_msb = 13
    byte_count_lsb = 14

class S(Enum):
    seven = 7
    eight = 8

class N(Enum):
    one = 1
    many = 2
