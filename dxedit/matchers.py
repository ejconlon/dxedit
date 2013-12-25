
def match_equals(x):
    def f(y):
        if x == y:
            return [x]
        else:
            return None
    return f

def match_any():
    def f(y):
        return [y]
    return f

def match_like(p):
    if len(p) != 8:
        raise Exception("Need 8bit pattern")
    m = {}
    ret_mask = 0
    for i in range(8):
        j = 7-i
        mask = 1 << i
        c = p[j]
        if c == '0':
            m[mask] = False
        elif c == '1':
            m[mask] = True
        else:
            ret_mask |= mask
    def f(y):
        for (mask, should_be_nonzero) in m.items():
            is_nonzero = mask & y != 0
            if is_nonzero != should_be_nonzero:
                return None
        return [ret_mask & y] # TODO
    return f

def match_seven():
    return match_like('0vvvvvvv')

def match_many(match_one):
    def f(ys):
        rs = []
        for y in ys:
            r = match_one(y)
            if r is None:
                return None
            else:
                rs.extend(r)
        return rs
