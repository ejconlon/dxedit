#!/usr/bin/env python3

import dxedit.constants
import dxedit.util
import dxedit.messages

def read_seqs(filename):
    data = None

    with open(filename, 'rb') as f:
        data = f.read()

    data = [k for k in data]

    starts = dxedit.util.all_indices(dxedit.constants.start_tag, data)
    ends =  dxedit.util.all_indices(dxedit.constants.end_tag, data)

    assert len(starts) == len(ends)

    zips = [z[:] for z in zip(starts, ends)]

    dxedit.util.assert_contiguous(data, zips)

    return [data[i:j+1] for (i, j) in zips]

def print_seqs(seqs):
    i = 0
    for seq in seqs:
        print("%4d" % i, " ".join(["%02x" % k for k in seq]))
        i += 1

if __name__ == "__main__":
    seqs = read_seqs('DX7Class.syx')
    print_seqs(seqs)

