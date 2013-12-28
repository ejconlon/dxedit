
# Range(name, Range(low, high)]
from collections import namedtuple
from .enum import Enum

# what is the byte relation? Single byte quantity, msb, lsb?
class Rel(Enum):
    ONE = 1
    MSB = 2
    LSB = 3

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

Row = namedtuple('Row', 'name rel matcher')

Table = namedtuple('Table', 'name size rows')

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
    Row("RESERVED", Rel.ONE, Range(0x00, 0x00)),
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
    Row("RESERVED", Rel.ONE, Range(0x00, 0x00)),
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
    Row("RESERVED", Rel.ONE, Range(0x00, 0x00)),
    Row("RESERVED", Rel.ONE, Range(0x00, 0x00)),
    Row("RESERVED", Rel.ONE, Range(0x00, 0x00)),
    Row("RESERVED", Rel.ONE, Range(0x00, 0x00))
] +\
sixteen("Step Seq Note", Rel.ONE, Range(0x00, 0xF7)) +\
sixteen("Step Seq Velocity", Rel.ONE, Range(0x00, 0xF7)) +\
sixteen("Step Seq Gate Time", Rel.LSB, Range(0x00, 0xF7)) +\
sixteen("Step Seq Control Change", Rel.LSB, Range(0x00, 0xF7)) +\
sixteen("Step Seq Gate Time", Rel.MSB, Range(0x00, 0xF7)) +\
sixteen("Step Seq Mute", Rel.ONE, Options(0x00, 0x01))
)

def get_table(hi, mid, low):
    if low != 0:
        return None
    if hi == 0x20:
        return table_1_9
    elif hi == 0x21:
        return table_1_10
    elif hi == 0x40 or hi == 0x41:
        return table_1_11_and_12
    elif hi in range(0x30, 0x40):
        return table_1_13
    elif hi == 0x50:
        return table_1_14
    else:
        return None

