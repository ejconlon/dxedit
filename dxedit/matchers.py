
def match_equals(x):
    def f(y):
        if x == y:
            return [x]
        else:
            return None
    f

def match_any():
    def f(y):
        return [y]

def match_like(p):
    if len(p) != 8:
        raise Exception("Need 8bit pattern")
    def f(y):
        return [y] # TODO

def match_seven():
    return match_like('0vvvvvvv')
