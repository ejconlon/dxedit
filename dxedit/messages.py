from .enums import *
from .matchers import *
from .constants import *

# matcher(byte): byte or None
# message_specs: { message_type : [(B, N, matcher(byte))] }
# Each spec is a list of sections that can have at most one "N.many" section.
# (This restriction eliminates the need for backtracking.)
message_specs = {
    'dx_param_change': [
        (B.sysex_start, N.one, match_equals(start_tag)),
        (B.mfr_id, N.one, match_equals(yamaha_mfr_id)),
        (B.device_num, N.one, match_like('0001nnnn')),
        (B.parameter_group_num, N.one, match_seven()),
        (B.parameter_num, N.one, match_seven()),
        (B.data, N.one, match_seven()),
        (B.sysex_end, N.one, match_equals(end_tag))
    ],

    'native_bulk_dump': [
        (B.sysex_start, N.one, match_equals(start_tag)),
        (B.mfr_id, N.one, match_equals(yamaha_mfr_id)),
        (B.device_num, N.one, match_like('0000nnnn')),
        (B.model_id, N.one, match_equals(native_bulk_dump_model_id)),
        (B.byte_count_msb, N.one, match_seven()),
        (B.byte_count_lsb, N.one, match_seven()),
        (B.addr_high, N.one, match_seven()),
        (B.addr_mid, N.one, match_seven()),
        (B.addr_low, N.one, match_seven()),
        (B.data, N.many, match_seven()),
        (B.checksum, N.one, match_seven()),
        (B.sysex_end, N.one, match_equals(end_tag))
    ]
}

# seq: [byte]
# returns: (message_type, [(B, [byte])]) or None
def parse_seq(seq):
    for (message_type, spec) in message_specs:
        parsed = parse_seq_with_spec(seq, spec)
        if parsed is not None:
            return (message_type, parsed)
    return None

def num_many(spec):
    m = 0
    for section in spec:
        if section[1] == N.many:
            m += 1
    return m

def validate_specs(specs):
    for spec in specs.values():
        m = num_many(spec)
        if m > 1:
            raise Exception("Specs may only have one many section")

validate_specs(message_specs)

def parse_seq_with_spec(seq, spec):
    m = num_many(spec)
    # Don't bother matching sequences that are too short to satisfy
    if len(seq) < len(spec) - m:
        return None
    rs = []
    seq_i = 0
    spec_i = 0
    while spec_i < len(spec):
        section = spec[spec_i]
        if section[1] == N.many:
            # start from back now, then match the many
            break
        r = match(seq[seq_i], section)
        if r is None:
            return None
        else:
            rs.append(r)
        seq_i += 1
        spec_i += 1
    # if we broke early for a many section,
    if spec_i < len(spec):
        raise Exception("TODO")

def match(byte, section):
    assert section[1] == N.one
    r = section[2](byte)
    if r is None:
        return None
    else:
        return (section[0], r)

