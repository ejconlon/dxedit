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
        msb = lookup(B.byte_count_msb, pseq)
        lsb = lookup(B.byte_count_lsb, pseq)
        data = lookup(B.data, pseq)
        assert all_none_or_not(msb, lsb, data)
        if data is not None:
            l = len(data[1])
            s = msb[1][0] << 7 | lsb[1][0]
            assert l == s

if __name__ == "__main__":
    seqs = read_seqs('DX7Class.syx')
    pseqs = parse_seqs(seqs)
    check_pseqs(pseqs)
    print_pseqs(pseqs)

