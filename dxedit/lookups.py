
# Range(name, Range(low, high)]
from collections import namedtuple
from .enum import Enum

# what is the byte relation? Single byte quantity, msb, lsb?
class Rel(Enum):
    ONE = 1
    MSB = 2
    LSB = 3

class Tag(Enum):
    PATTERN = 1
    PART = 2
    SONG = 3
    MEASURE = 4

class Range:
    def __init__(self, start, end):
        self.start = start
        self.end = end

    def __contains__(self, value):
        return value >= self.start and value <= self.end

class Options:
    def __init__(self, *opts):
        self.opts = set(opts)

    def __contains__(self, value):
        return value in self.opts

class Multi:
    def __init__(self, *ranges):
        self.ranges = ranges

    def __contains__(self, value):
        return any(value in r for r in self.ranges)

Row = namedtuple('Row', 'name rel matcher')

Table = namedtuple('Table', 'name size rows')

AnnoTable = namedtuple('AnnoTable', 'anno table')

table_1_9 = Table("User Pattern Voice Common 1", 0x29, [
    Row("Distortion: Off/On", Rel.ONE, Options(0x00, 0x01)),
    Row("Distortion: Drive", Rel.ONE, Range(0x00, 0x64)),
    Row("Distortion: AMP Type", Rel.ONE, Range(0x00, 0x03)),
    Row("Distortion: LPF Cutoff", Rel.ONE, Range(0x22, 0x3C)),
    Row("Distortion: Out Level", Rel.ONE, Range(0x00, 0x64)),
    Row("Distortion: Dry/Wet", Rel.ONE, Range(0x01, 0x7F)),
    Row("2-Band EQ Low Freq", Rel.ONE, Range(0x04, 0x28)),
    Row("2-Band EQ Low Gain", Rel.ONE, Range(0x34, 0x4C)),
    Row("2-Band EQ Mid Freq", Rel.ONE, Range(0x0E, 0x36)),
    Row("2-Band EQ Mid Gain", Rel.ONE, Range(0x34, 0x4C)),
    Row("2-Band EQ Mid Resonance(Q)", Rel.ONE, Range(0x0A, 0x78)),
    Row("RESERVED", Rel.ONE, Options(0x00)),
    Row("Filter Cutoff", Rel.ONE, Range(0x00, 0x7F)),
    Row("Filter Resonance(Q)", Rel.ONE, Range(0x00, 0x74)),
    Row("Filter Type", Rel.ONE, Range(0x00, 0x05)),
    Row("Filter Cutoff Scaling Depth", Rel.ONE, Range(0x00, 0x7F)),
    Row("Filter Cutoff Modulation Depth", Rel.ONE, Range(0x00, 0x63)),
    Row("Filter Input Gain", Rel.ONE, Range(0x34, 0x4C)),
    Row("FEG Attack", Rel.ONE, Range(0x00, 0x7F)),
    Row("FEG Decay", Rel.ONE, Range(0x00, 0x7F)),
    Row("FEG Sustain", Rel.ONE, Range(0x00, 0x7F)),
    Row("FEG Release", Rel.ONE, Range(0x00, 0x7F)),
    Row("FEG Depth Velocity Sense", Rel.ONE, Range(0x00, 0x7F)),
    Row("RESERVED", Rel.ONE, Options(0x00)),
    Row("Noise OSC Type", Rel.ONE, Range(0x00, 0x0F)),
    Row("Mixer Voice Level", Rel.ONE, Range(0x00, 0x7F)),
    Row("Mixer Noise Level", Rel.ONE, Range(0x00, 0x7F)),
    Row("Modulator 1 Harmonic", Rel.ONE, Range(0x00, 0x7F)),
    Row("Modulator 2 Harmonic", Rel.ONE, Range(0x00, 0x7F)),
    Row("Modulator 3 Harmonic", Rel.ONE, Range(0x00, 0x7F)),
    Row("Modulator 1 FM Depth", Rel.ONE, Range(0x00, 0x7F)),
    Row("Modulator 2 FM Depth", Rel.ONE, Range(0x00, 0x7F)),
    Row("Modulator 3 FM Depth", Rel.ONE, Range(0x00, 0x7F)),
    Row("Modulator 1 EG Decay", Rel.ONE, Range(0x00, 0x7F)),
    Row("Modulator 2 EG Decay", Rel.ONE, Range(0x00, 0x7F)),
    Row("Modulator 3 EG Decay", Rel.ONE, Range(0x00, 0x7F)),
    Row("AEG Attack", Rel.ONE, Range(0x00, 0x7F)),
    Row("AEG Sustain", Rel.ONE, Range(0x00, 0x7F)),
    Row("AEG Release", Rel.ONE, Range(0x00, 0x7F))
])

table_1_10 = Table("User Pattern Voice Common 2", 0x05, [
    Row("Modulator Select", Rel.ONE, Range(0x00, 0x03)),
    Row("Scene Control", Rel.ONE, Range(0x00, 0x7F)),
    Row("Common Tempo", Rel.MSB, Range(0x00, 0x4A)),
    Row("Common Tempo", Rel.LSB, Range(0x00, 0x7F)),
    Row("Play Effect Swing", Rel.ONE, Range(0x32, 0x53))
])

table_1_11_and_12 = Table("User Patter Voice Scene 1", 0x1C, [
    Row("Filter Cutoff", Rel.ONE, Range(0x00, 0x7F)),
    Row("Filter Resonance(Q)", Rel.ONE, Range(0x00, 0x74)),
    Row("FEG Attack", Rel.ONE, Range(0x00, 0x7F)),
    Row("FEG Decay", Rel.ONE, Range(0x00, 0x7F)),
    Row("FEG Sustain", Rel.ONE, Range(0x00, 0x7F)),
    Row("FEG Release", Rel.ONE, Range(0x00, 0x7F)),
    Row("FEG Depth", Rel.ONE, Range(0x00, 0x7F)),
    Row("Filter Type", Rel.ONE, Range(0x00, 0x05)),
    Row("LFO Speed", Rel.ONE, Range(0x00, 0x63)),
    Row("Portamento Time", Rel.ONE, Range(0x00, 0x63)),
    Row("Mixer Noise Level", Rel.ONE, Range(0x00, 0x7F)),
    Row("Modulator 1 Harmonic", Rel.ONE, Range(0x00, 0x7F)),
    Row("Modulator 2 Harmonic", Rel.ONE, Range(0x00, 0x7F)),
    Row("Modulator 3 Harmonic", Rel.ONE, Range(0x00, 0x7F)),
    Row("Modulator 1 FM Depth", Rel.ONE, Range(0x00, 0x7F)),
    Row("Modulator 2 FM Depth", Rel.ONE, Range(0x00, 0x7F)),
    Row("Modulator 3 FM Depth", Rel.ONE, Range(0x00, 0x7F)),
    Row("Modulator 1 EG Decay", Rel.ONE, Range(0x00, 0x7F)),
    Row("Modulator 2 EG Decay", Rel.ONE, Range(0x00, 0x7F)),
    Row("Modulator 3 EG Decay", Rel.ONE, Range(0x00, 0x7F)),
    Row("AEG Attack", Rel.ONE, Range(0x00, 0x7F)),
    Row("AEG Decay", Rel.ONE, Range(0x00, 0x7F)),
    Row("AEG Sustain", Rel.ONE, Range(0x00, 0x7F)),
    Row("AEG Release", Rel.ONE, Range(0x00, 0x7F)),
    Row("Volume", Rel.ONE, Range(0x00, 0x7F)),
    Row("Pan", Rel.ONE, Range(0x00, 0x7F)),
    Row("Effect Send", Rel.ONE, Range(0x00, 0x7F)),
    Row("Effect Parameter", Rel.ONE, Range(0x00, 0x7F))
])

def flatten(xss):
    ys = []
    for xs in xss:
        ys.extend(xs)
    return ys

track_params = flatten([[
    Row("Free EG Track Param "+str(i), Rel.ONE, Range(0x00, 0x1F)),
    Row("Free EG Track Scene Switch "+str(i), Rel.ONE, Options(0x00, 0x01))]
    for i in range(1, 4 + 1)
])

track_datas = flatten([flatten([
    Row("Free EG Track "+str(i)+" Data "+str(j), Rel.MSB, Range(0x00, 0x01)),
    Row("Free EG Track "+str(i)+" Data "+str(j), Rel.LSB, Range(0x00, 0x7F))]
    for j in range(1, 192 + 1))
    for i in range(1, 4 + 1)
])

table_1_13 = Table("User Pattern Voice Free EG", 0x60C, [
    Row("Free EG Trigger", Rel.ONE, Range(0x00, 0x03)),
    Row("Free EG Loop Type", Rel.ONE, Range(0x00, 0x04)),
    Row("Free EG Length", Rel.ONE, Range(0x02, 0x60)),
    Row("Free EG Keyboard Track", Rel.ONE, Range(0x00, 0x7F))
] + track_params + track_datas)

def sixteen(name, rel, rang):
    return [Row(name + " " + str(i), rel, rang) for i in range(1, 16 + 1)]

table_1_14 = Table("User Pattern Step Seq Pattern", 0x66, [
    Row("Step Seq Base Unit", Rel.ONE, Options(0x04, 0x06, 0x07)),
    Row("Step Seq Length", Rel.ONE, Options(0x08, 0x0C, 0x10)),
    Row("RESERVED", Rel.ONE, Options(0x00)),
    Row("RESERVED", Rel.ONE, Options(0x00)),
    Row("RESERVED", Rel.ONE, Options(0x00)),
    Row("RESERVED", Rel.ONE, Options(0x00))
] +\
sixteen("Step Seq Note", Rel.ONE, Range(0x00, 0xF7)) +\
sixteen("Step Seq Velocity", Rel.ONE, Range(0x00, 0xF7)) +\
sixteen("Step Seq Gate Time", Rel.LSB, Range(0x00, 0xF7)) +\
sixteen("Step Seq Control Change", Rel.LSB, Range(0x00, 0xF7)) +\
sixteen("Step Seq Gate Time", Rel.MSB, Range(0x00, 0xF7)) +\
sixteen("Step Seq Mute", Rel.ONE, Options(0x00, 0x01))
)

table_4_2 = Table("System 1", 0x09, [
    Row("Synth Receive Channel", Rel.ONE, Options(*(list(range(16)) + [0x7F]))),
    Row("Rhythm 1 Receive Channel", Rel.ONE, Options(*(list(range(16)) + [0x7F]))),
    Row("Rhythm 2 Receive Channel", Rel.ONE, Options(*(list(range(16)) + [0x7F]))),
    Row("Rhythm 3 Receive Channel", Rel.ONE, Options(*(list(range(16)) + [0x7F]))),
    Row("RESERVED", Rel.ONE, Options(0x00)),
    Row("RESERVED", Rel.ONE, Options(0x00)),
    Row("RESERVED", Rel.ONE, Options(0x00)),
    Row("Play Effect Gate Time", Rel.ONE, Range(0x01, 0xC8)),
    Row("Step Seq Loop Type", Rel.ONE, Options(0x00, 0x01, 0x02, 0x03))
])

table_4_3_and_7 = Table("Current Effect", 0x03, [
    Row("Effect Type MSB", Rel.ONE, Options(0x00, 0x01, 0x02, 0x03)), # Not MSB rel b/c of lookup table
    Row("Effect Type LSB", Rel.ONE, Options(0x00, 0x01, 0x02, 0x03)), # Not LSB rel b/c of lookup table
    Row("Effect Parameter", Rel.ONE, Range(0x00, 0x7F))
])

table_4_8_and_9 = Table("Multi Part", 0x0F, [
    Row("RESERVED", Rel.ONE, Options(0x00)),
    Row("RESERVED", Rel.ONE, Options(0x00)),
    Row("RESERVED", Rel.ONE, Options(0x00)),
    Row("RESERVED", Rel.ONE, Options(0x00)),
    Row("RESERVED", Rel.ONE, Options(0x00)),
    Row("Volume", Rel.ONE, Range(0x00, 0x7F)),
    Row("Pan", Rel.ONE, Range(0x00, 0x7F)),
    Row("Effect 1 Send", Rel.ONE, Range(0x00, 0x7F)),
    Row("RESERVED", Rel.ONE, Options(0x00)),
    Row("RESERVED", Rel.ONE, Options(0x00)),
    Row("Filter Cutoff Frequency", Rel.ONE, Range(0x00, 0x7F)),
    Row("Filter Resonance", Rel.ONE, Range(0x00, 0x7F)),
    Row("RESERVED", Rel.ONE, Options(0x00)),
    Row("RESERVED", Rel.ONE, Options(0x00)),
    Row("RESERVED", Rel.ONE, Options(0x00))
])

table_4_10_and_11 = Table("Rhythm Track Current Step Seq Pattern", 0x66, [
    Row("RESERVED", Rel.ONE, Options(0x00)),
    Row("RESERVED", Rel.ONE, Options(0x00)),
    Row("RESERVED", Rel.ONE, Options(0x00)),
    Row("RESERVED", Rel.ONE, Options(0x00)),
    Row("RESERVED", Rel.ONE, Options(0x00))
] +\
sixteen("Step Seq Instrument", Rel.ONE, Range(0x00, 0x78)) +\
sixteen("Step Seq Velocity", Rel.ONE, Range(0x00, 0x7F)) +\
sixteen("Step Seq Gate Time", Rel.LSB, Range(0x00, 0x7F)) +\
sixteen("Step Seq Pitch", Rel.ONE, Range(0x00, 0x7F)) +\
sixteen("Step Seq Gate Time", Rel.MSB, Range(0x00, 0x07)) +\
sixteen("Step Seq Mute", Rel.ONE, Options(0x00, 0x01))
)

table_4_12_and_13 = Table("User Song", 0x0B, [
    # TODO range for all these 2-bytes
    Row("Pattern Num", Rel.MSB, Range(0x00, 0x7F)),
    Row("Pattern Num", Rel.LSB, Range(0x00, 0x7F)),
    Row("BPM", Rel.MSB, Range(0x00, 0x7F)),
    Row("BPM", Rel.LSB, Range(0x00, 0x7F)),
    Row("Play FX Gate Time", Rel.MSB, Range(0x00, 0x7F)),
    Row("Play FX Gate Time", Rel.LSB, Range(0x00, 0x7F)),
    Row("Beat", Rel.ONE, Options(0x00, 0x01, 0x02, 0x03, 0x7F)),
    Row("Swing", Rel.ONE, Multi(Range(0x32, 0x53), Options(0x7F))),
    Row("Pitch", Rel.ONE, Multi(Range(0x28, 0x58), Options(0x7F))),
    Row("Loop Type", Rel.ONE, Options(0x00, 0x01, 0x7F)),
    Row("Track Mute", Rel.ONE, Multi(Range(0x00, 0x0F), Options(0x7F))),
])

def get_table(model_id, hi, mid, low):
    if low != 0:
        return None
    if model_id == 0x62:
        anno = { Tag.PATTERN: mid }
        if hi == 0x20:
            return AnnoTable(anno, table_1_9)
        elif hi == 0x21:
            return AnnoTable(anno, table_1_10)
        elif hi == 0x40 or hi == 0x41:
            return AnnoTable(anno, table_1_11_and_12)
        elif hi in range(0x30, 0x40):
            return AnnoTable(anno, table_1_13)
        elif hi == 0x50:
            return AnnoTable(anno, table_1_14)
    elif model_id == 0x6D:
        if hi in range(0x20, 0x30):
            anno = { Tag.PART: (hi & 0x0F), Tag.PATTERN: mid }
            return AnnoTable(anno, table_4_10_and_11)
        elif hi == 0x30:
            anno = { Tag.PATTERN: mid }
            return AnnoTable(anno, table_4_3_and_7)
        elif hi in range(0x40, 0x50):
            anno = { Tag.PART: (hi & 0x0F), Tag.PATTERN: mid }
            return AnnoTable(anno, table_4_8_and_9)
        elif hi in range(0x60, 0x70):
            anno = { Tag.SONG: (hi & 0x0F), Tag.MEASURE: mid }
            return AnnoTable(anno, table_4_12_and_13)
        elif hi in range(0x70, 0x80):
            anno = { Tag.SONG: (hi & 0x0F), Tag.MEASURE: (mid + 0x7F) }
            return AnnoTable(anno, table_4_12_and_13)
    return None

