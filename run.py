#!/usr/bin/env python3

from collections import defaultdict
from dxedit.constants import *
from dxedit.util import *
from dxedit.messages import *
from dxedit.enums import *

def read_seqs(filename):
    data = None

    with open(filename, 'rb') as f:
        data = f.read()

    data = [k for k in data]

    starts = all_indices(start_tag, data)
    ends =  all_indices(end_tag, data)

    assert len(starts) == len(ends)

    zips = [z[:] for z in zip(starts, ends)]

    assert_contiguous(data, zips)

    return [data[i:j+1] for (i, j) in zips]

def parse_seqs(seqs):
    return [parse_seq(seq) for seq in seqs]

def print_pseqs(pseqs):
    i = 0
    types = defaultdict(lambda: 0)
    for pseq in pseqs:
        types[pseq[0]] += 1
        print(pseq)
        i += 1
    print(types)

def check_pseqs(pseqs):
    for pseq in pseqs:
        check_size(pseq)
        check_checksum(pseq)

def check_size(pseq):
    msb = lookup(B.byte_count_msb, pseq[1])
    lsb = lookup(B.byte_count_lsb, pseq[1])
    data = lookup(B.data, pseq[1])
    if data is not None and all_not_none(msb, lsb):
        l = len(data)
        s = msb[0] << 7 | lsb[0]
        assert l == s
    else:
        assert all_none(msb, lsb)

def check_checksum(pseq):
    data = lookup(B.data, pseq[1])
    checksum = lookup(B.checksum, pseq[1])
    if pseq[0] == 'dx_bulk_dump':
        # checksum is (sum(data))
        assert all_not_none(data, checksum)
        test = ((0xFF ^ sum(data)) + 1) & 0x7F
        assert test == checksum[0]
    elif pseq[0] == 'dx200_native_bulk_dump':
        # checksum is (address + count + sum(data))
        high = lookup(B.addr_high, pseq[1])
        mid = lookup(B.addr_mid, pseq[1])
        low = lookup(B.addr_low, pseq[1])
        msb = lookup(B.byte_count_msb, pseq[1])
        lsb = lookup(B.byte_count_lsb, pseq[1])
        assert all_not_none(data, checksum, high, mid, low, msb, lsb)
        test = ((0xFF ^ (high[0] + mid[0] + low[0] + msb[0] + lsb[0] + sum(data))) + 1) & 0x7F
        assert test == checksum[0]
    else:
        assert checksum is None

if __name__ == "__main__":
    seqs = read_seqs('DX7Class.syx')
    pseqs = parse_seqs(seqs)
    check_pseqs(pseqs)
    print_pseqs(pseqs)

