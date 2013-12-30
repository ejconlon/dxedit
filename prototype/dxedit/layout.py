
from collections import namedtuple

"""
song = [pattern numbers]
pattern = (options, voice, voice seq, 3x rhythm seqs, effect)
"""

Pattern = namedtuple('Pattern', [
    'number', 'options', 'effect', 'voice', 'voice_seq',
    'rhythm_seq_1', 'rhythm_seq_2', 'rhythm_seq_3'])

