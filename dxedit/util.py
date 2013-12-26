
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

def lookup(key, assocs):
    for item in assocs:
        if item[0] == key:
            return item[1]
    return None

def all_none(*xs):
    return all(x is None for x in xs)

def all_not_none(*xs):
    return all(x is not None for x in xs)

