#!/usr/bin/env python3

from collections import defaultdict
from dxedit.constants import *
from dxedit.util import *
from dxedit.messages import *
from dxedit.enums import *
from dxedit.pseqs import *

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
        check_count(pseq)
        check_checksum(pseq)
        print_addr(pseq)

def print_addr(pseq):
    if pseq[0] == T.dx200_native_bulk_dump:
        model_id = lookup(B.model_id, pseq[1])
        addr_high = lookup(B.addr_high, pseq[1])
        addr_mid = lookup(B.addr_mid, pseq[1])
        addr_low = lookup(B.addr_low, pseq[1])
        data = lookup(B.data, pseq[1])
        printhex(model_id, addr_high, addr_mid, addr_low, data)
        print(len(data), hex(len(data)))

def printhex(*arrs):
    s = ""
    for arr in arrs:
        s += "[" + ",".join(hex(x) for x in arr) + "]"
        s += " "
    print(s)

if __name__ == "__main__":
    seqs = read_seqs('DX7Class.syx')
    pseqs = parse_seqs(seqs)
    check_pseqs(pseqs)
    #print_pseqs(pseqs)

