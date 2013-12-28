
from .enums import *
from .util import *
from .lookups import *

# return (msb, lsb) byte count for data of length l
def make_count_bytes(l):
    lsb = l & 0x7F
    msb = l >> 7
    return (msb, lsb)

def check_count(pseq):
    msb = lookup(B.byte_count_msb, pseq[1])
    lsb = lookup(B.byte_count_lsb, pseq[1])
    data = lookup(B.data, pseq[1])
    if data is not None and all_not_none(msb, lsb):
        l = len(data)
        s = msb[0] << 7 | lsb[0]
        assert l == s
        bs = make_count_bytes(s)
        assert msb[0] == bs[0]
        assert lsb[0] == bs[1]
    else:
        assert all_none(msb, lsb)

# return checksum or None if inappropriate type
def make_checksum(pseq):
    data = lookup(B.data, pseq[1])
    checksum = lookup(B.checksum, pseq[1])
    if pseq[0] == T.dx_bulk_dump:
        # checksum is (sum(data))
        assert all_not_none(data, checksum)
        return ((0xFF ^ sum(data)) + 1) & 0x7F
    elif pseq[0] == T.dx200_native_bulk_dump:
        # checksum is (address + count + sum(data))
        high = lookup(B.addr_high, pseq[1])
        mid = lookup(B.addr_mid, pseq[1])
        low = lookup(B.addr_low, pseq[1])
        msb = lookup(B.byte_count_msb, pseq[1])
        lsb = lookup(B.byte_count_lsb, pseq[1])
        assert all_not_none(data, checksum, high, mid, low, msb, lsb)
        return ((0xFF ^ (high[0] + mid[0] + low[0] + msb[0] + lsb[0] + sum(data))) + 1) & 0x7F
    else:
        return None

def check_checksum(pseq):
    actual = make_checksum(pseq)
    expected = lookup(B.checksum, pseq[1])
    if expected is None:
        assert actual is None
    else:
        assert actual == expected[0]

def get_table_by_pseq(pseq):
    if pseq[0] == T.dx200_native_bulk_dump:
        high = lookup(B.addr_high, pseq[1])
        mid = lookup(B.addr_mid, pseq[1])
        low = lookup(B.addr_low, pseq[1])
        assert all_not_none(high, mid, low)
        return get_table(high[0], mid[0], low[0])
    else:
        return None

def check_tables(pseq):
    if pseq[0] == T.dx200_native_bulk_dump:
        table = get_table_by_pseq(pseq)
        if table == None:
            print(pseq)
            raise Exception('no table')
