from .enum import Enum

class T(Enum):
    dx200_native_bulk_dump = 1
    dx_bulk_dump = 2
    dx200_native_param_change = 3
    dx_param_change = 4

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
    format_num = 15

class S(Enum):
    seven = 7
    eight = 8

class N(Enum):
    one = 1
    many = 2
