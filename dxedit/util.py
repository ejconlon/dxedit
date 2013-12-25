
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

def assert_contiguous(data, zips):
    l = len(data)
    i = -1
    for z in zips:
        assert z[0] == i + 1
        i = z[1]
    assert i == l - 1
