#!/usr/bin/env python

data = None
with open('DX7Class.syx', 'rb') as f:
    data = f.read()

data = [ord(k) for k in data]

start_tag = 0xF0
end_tag = 0xF7

def all_indices(value, qlist):
    indices = []
    idx = -1
    while True:
        try:
            idx = qlist.index(value, idx+1)
            indices.append(idx)
        except ValueError:
            break
    return indices

starts = all_indices(start_tag, data)
ends =  all_indices(end_tag, data)

assert len(starts) == len(ends)

zips = zip(starts, ends)

def assert_contiguous(data, zips):
    l = len(data)
    i = -1
    for z in zips:
        assert z[0] == i + 1
        i = z[1]
    assert i == l - 1

assert_contiguous(data, zips)

seqs = [data[i:j+1] for (i, j) in zips]

def print_seqs(seqs):
    i = 0
    for seq in seqs:
        print "%4d" % i, " ".join(["%02x" % k for k in seq])
        print parse(seq)
        i += 1

class BaseMessage(object):
    def __init__(self, mfr_id, device_num, model_id, checksum, unparsed):
        self.mfr_id = mfr_id
        self.device_num = device_num
        self.model_id = model_id
        self.checksum = checksum
        self.unparsed = unparsed

    def __repr__(self):
        bs = [start_tag, self.mfr_id, self.device_num, self.model_id]
        bs.extend(self.unparsed)
        bs.extend([self.checksum, end_tag])
        return " ".join("%02x" % x for x in bs)

def parse(seq):
    assert seq[0] == start_tag
    assert seq[-1] == end_tag
    mfr_id = seq[1]
    device_num = seq[2]
    model_id = seq[3]
    unparsed = seq[4:-2]
    checksum = seq[-2]
    return BaseMessage(mfr_id, device_num, model_id, checksum, unparsed)


print_seqs(seqs)

