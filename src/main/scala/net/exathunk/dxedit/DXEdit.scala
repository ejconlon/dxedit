
object DXEdit {

  // Import into SMALL SCOPES to avoid toByte'ing all the literal bytes there
  object ImplicitIntToByte {
    implicit def intToByte(int: Int): Byte = int.toByte
  }

  case object TodoException extends RuntimeException

  type Try[A] = Either[Throwable, A]
  type Frame = Seq[Byte]
  type SubFrame = Seq[Byte]
  type Data = Seq[Byte]

  def extractFrames(bytes: Seq[Byte]): Try[Seq[Frame]] = throw TodoException

  trait FrameReader {
    def readPSeq(frame: Frame): Try[PSeq]
  }

  object FrameType extends Enumeration {
    type FrameType = Value
    // These order message parsing so be careful
    val DX200_NATIVE_BULK_DUMP = Value
    val DX_BULK_DUMP = Value
    val DX200_NATIVE_PARAM_CHANGE = Value
    val DX_PARAM_CHANGE = Value
  }

  object SubFrameType extends Enumeration {
    type SubFrameType = Value
    val SYSEX_START = Value
    val SYSEX_END = Value
    val MFR_ID = Value
    val DEVICE_NUM = Value
    val MODEL_ID = Value
    val CHECKSUM = Value
    val ADDR_HIGH = Value
    val ADDR_MID = Value
    val ADDR_LOW = Value
    val DATA = Value
    val PARAMETER_GROUP_NUM = Value
    val PARAMETER_NUM = Value
    val BYTE_COUNT_MSB = Value
    val BYTE_COUNT_LSB = Value
    val FORMAT_NUM = Value
  }

  object CountType extends Enumeration {
    type CountType = Value
    val SEVEN = Value
    val EIGHT = Value
  }

  object RepeatType extends Enumeration {
    type RepeatType = Value
    val ONCE = Value
    val MANY = Value
  }

  object RelType extends Enumeration {
    type RelType = Value
    val ONE = Value
    val MSB = Value
    val LSB = Value
  }

  object AnnoType extends Enumeration {
    type AnnoType = Value
    val PATTERN = Value
    val PART = Value
    val SONG = Value
    val MEASURE = Value
  }

  object DataType extends Enumeration {
    type DataType = Value
    val VOICE_COMMON_1 = Value
    val VOICE_COMMON_2 = Value
    val VOICE_SCENE = Value
    val VOICE_FREE_EG = Value
    val VOICE_STEP_SEQ = Value
    val EFFECT = Value
    val PART_MIX = Value
    val RHYTHM_STEP_SEQ = Value
    val SONG_DATA = Value
  }

  object Constants {
    import ImplicitIntToByte._
    val START_TAG: Byte = 0xF0
    val END_TAG: Byte = 0xF7
    val YAMAHA_MFR_ID: Byte = 0x43
    val SYSTEM1_MODEL_ID: Byte = 0x62
    val SYSTEM2_MODEL_ID: Byte = 0x6D
  }

  import FrameType._
  import SubFrameType._
  import CountType._
  import RepeatType._
  import RelType._
  import AnnoType._
  import DataType._

  case class LookupTable[N, Row](name: N, sizeForCheck: Int, rows: Seq[Row])

  trait ByteRange {
    def contains(byte: Byte): Boolean
    def size: Int
  }

  object ByteRange {
    case class Interval(start: Byte, end: Byte) extends ByteRange {
      override def contains(byte: Byte): Boolean =
        byte >= start && byte <= end
      override def size: Int = (end - start).toInt
    }

    case class Discrete(set: Set[Byte]) extends ByteRange {
      override def contains(byte: Byte): Boolean = set.contains(byte)
      override def size: Int = set.size
    }

    case class Union(a: ByteRange, b: ByteRange) extends ByteRange {
      override def contains(byte: Byte): Boolean =
        a.contains(byte) || b.contains(byte)
      override def size: Int = a.size + b.size
    }
  }

  type ByteMatcher = Byte => Try[Byte]

  type FrameRow = (SubFrameType, RepeatType, ByteMatcher)
  type FrameTable = LookupTable[FrameType, FrameRow]
  val FrameTable = LookupTable

  type DataRow = (String, RelType, ByteRange)
  type DataTable = LookupTable[DataType, DataRow]
  val DataTable = LookupTable


  object ByteMatchers {
    def matchEquals(byte: Byte): ByteMatcher = throw TodoException
    def matchLike(pattern: String): ByteMatcher = throw TodoException
    def matchSeven: ByteMatcher = throw TodoException
    def matchOneOf(a: ByteMatcher, b: ByteMatcher): ByteMatcher = throw TodoException
  }

  object FirstPass {
    import ByteMatchers._
    import Constants._

    lazy val tables: Seq[FrameTable] = Seq(
      FrameTable(
        DX200_NATIVE_BULK_DUMP, 12,
        Seq(
          (SYSEX_START, ONCE, matchEquals(START_TAG)),
          (MFR_ID, ONCE, matchEquals(YAMAHA_MFR_ID)),
          (DEVICE_NUM, ONCE, matchLike("0000nnnn")),
          (MODEL_ID, ONCE, matchOneOf(matchEquals(SYSTEM1_MODEL_ID), matchEquals(SYSTEM2_MODEL_ID))),
          (BYTE_COUNT_MSB, ONCE, matchSeven),
          (BYTE_COUNT_LSB, ONCE, matchSeven),
          (ADDR_HIGH, ONCE, matchSeven),
          (ADDR_MID, ONCE, matchSeven),
          (ADDR_LOW, ONCE, matchSeven),
          (DATA, MANY, matchSeven),
          (CHECKSUM, ONCE, matchSeven),
          (SYSEX_END, ONCE, matchEquals(END_TAG))
        )
      ),
      FrameTable(
        DX_BULK_DUMP, 9,
        Seq(
          (SYSEX_START, ONCE, matchEquals(START_TAG)),
          (MFR_ID, ONCE, matchEquals(YAMAHA_MFR_ID)),
          (DEVICE_NUM, ONCE, matchLike("0000nnnn")),
          (FORMAT_NUM, ONCE, matchSeven),
          (BYTE_COUNT_MSB, ONCE, matchSeven),
          (BYTE_COUNT_LSB, ONCE, matchSeven),
          (DATA, MANY, matchSeven),
          (CHECKSUM, ONCE, matchSeven),
          (SYSEX_END, ONCE, matchEquals(END_TAG))
        )
      ),
      FrameTable(
        DX200_NATIVE_PARAM_CHANGE, 9,
        Seq(
          (SYSEX_START, ONCE, matchEquals(START_TAG)),
          (MFR_ID, ONCE, matchEquals(YAMAHA_MFR_ID)),
          (DEVICE_NUM, ONCE, matchLike("0001nnnn")),
          (MODEL_ID, ONCE, matchOneOf(matchEquals(SYSTEM1_MODEL_ID), matchEquals(SYSTEM2_MODEL_ID))),
          (ADDR_HIGH, ONCE, matchSeven),
          (ADDR_MID, ONCE, matchSeven),
          (ADDR_LOW, ONCE, matchSeven),
          (DATA, MANY, matchSeven),
          (SYSEX_END, ONCE, matchEquals(END_TAG))
        )
      ),
      FrameTable(
        DX_PARAM_CHANGE, 7,
        Seq(
          (SYSEX_START, ONCE, matchEquals(START_TAG)),
          (MFR_ID, ONCE, matchEquals(YAMAHA_MFR_ID)),
          (DEVICE_NUM, ONCE, matchLike("0001nnnn")),
          (PARAMETER_GROUP_NUM, ONCE, matchSeven),
          (PARAMETER_NUM, ONCE, matchSeven),
          (DATA, ONCE, matchSeven),
          (SYSEX_END, ONCE, matchEquals(END_TAG))
        )
      )
    )
  }

  object SecondPass {
    import ImplicitIntToByte._
    import ByteRange._

    lazy val tables: Seq[DataTable] = Seq(
      DataTable(
        VOICE_COMMON_1, 0x29,
        Seq(
          ("Distortion: Off/On", ONE, onOff),
          ("Distortion: Drive", ONE, Interval(0x00, 0x64)),
          ("Distortion: AMP Type", ONE, Interval(0x00, 0x03)),
          ("Distortion: LPF Cutoff", ONE, Interval(0x22, 0x3C)),
          ("Distortion: Out Level", ONE, Interval(0x00, 0x64)),
          ("Distortion: Dry/Wet", ONE, Interval(0x01, 0x7F)),
          ("2-Band EQ Low Freq", ONE, Interval(0x04, 0x28)),
          ("2-Band EQ Low Gain", ONE, Interval(0x34, 0x4C)),
          ("2-Band EQ Mid Freq", ONE, Interval(0x0E, 0x36)),
          ("2-Band EQ Mid Gain", ONE, Interval(0x34, 0x4C)),
          ("2-Band EQ Mid Resonance(Q)", ONE, Interval(0x0A, 0x78)),
          ("RESERVED 1", ONE, anySeven),
          ("Filter Cutoff", ONE, anySeven),
          ("Filter Resonance(Q)", ONE, Interval(0x00, 0x74)),
          ("Filter Type", ONE, Interval(0x00, 0x05)),
          ("Filter Cutoff Scaling Depth", ONE, anySeven),
          ("Filter Cutoff Modulation Depth", ONE, Interval(0x00, 0x63)),
          ("Filter Input Gain", ONE, Interval(0x34, 0x4C)),
          ("FEG Attack", ONE, anySeven),
          ("FEG Decay", ONE, anySeven),
          ("FEG Sustain", ONE, anySeven),
          ("FEG Release", ONE, anySeven),
          ("FEG Depth", ONE, anySeven),
          ("FEG Depth Velocity Sense", ONE, anySeven),
          ("RESERVED 2", ONE, anySeven),
          ("Noise OSC Type", ONE, Interval(0x00, 0x0F)),
          ("Mixer Voice Level", ONE, anySeven),
          ("Mixer Noise Level", ONE, anySeven),
          ("Modulator 1 Harmonic", ONE, anySeven),
          ("Modulator 2 Harmonic", ONE, anySeven),
          ("Modulator 3 Harmonic", ONE, anySeven),
          ("Modulator 1 FM Depth", ONE, anySeven),
          ("Modulator 2 FM Depth", ONE, anySeven),
          ("Modulator 3 FM Depth", ONE, anySeven),
          ("Modulator 1 EG Decay", ONE, anySeven),
          ("Modulator 2 EG Decay", ONE, anySeven),
          ("Modulator 3 EG Decay", ONE, anySeven),
          ("AEG Attack", ONE, anySeven),
          ("AEG Decay", ONE, anySeven),
          ("AEG Sustain", ONE, anySeven),
          ("AEG Release", ONE, anySeven)
        )
      ),
      DataTable(VOICE_COMMON_2, 0x05,
        Seq(
          ("Modulator Select", ONE, Interval(0x00, 0x03)),
          ("Scene Control", ONE, anySeven),
          ("Common Tempo", MSB, Interval(0x00, 0x4A)),
          ("Common Tempo", LSB, anySeven),
          ("Play Effect Swing", ONE, Interval(0x32, 0x53))
        )
      ),
      DataTable(VOICE_SCENE, 0x1C,
        Seq(
          ("Filter Cutoff", ONE, anySeven),
          ("Filter Resonance(Q)", ONE, Interval(0x00, 0x74)),
          ("FEG Attack", ONE, anySeven),
          ("FEG Decay", ONE, anySeven),
          ("FEG Sustain", ONE, anySeven),
          ("FEG Release", ONE, anySeven),
          ("FEG Depth", ONE, anySeven),
          ("Filter Type", ONE, Interval(0x00, 0x05)),
          ("LFO Speed", ONE, Interval(0x00, 0x63)),
          ("Portamento Time", ONE, Interval(0x00, 0x63)),
          ("Mixer Noise Level", ONE, anySeven),
          ("Modulator 1 Harmonic", ONE, anySeven),
          ("Modulator 2 Harmonic", ONE, anySeven),
          ("Modulator 3 Harmonic", ONE, anySeven),
          ("Modulator 1 FM Depth", ONE, anySeven),
          ("Modulator 2 FM Depth", ONE, anySeven),
          ("Modulator 3 FM Depth", ONE, anySeven),
          ("Modulator 1 EG Decay", ONE, anySeven),
          ("Modulator 2 EG Decay", ONE, anySeven),
          ("Modulator 3 EG Decay", ONE, anySeven),
          ("AEG Attack", ONE, anySeven),
          ("AEG Decay", ONE, anySeven),
          ("AEG Sustain", ONE, anySeven),
          ("AEG Release", ONE, anySeven),
          ("Volume", ONE, anySeven),
          ("Pan", ONE, anySeven),
          ("Effect Send", ONE, anySeven),
          ("Effect Parameter", ONE, anySeven)
        )
      ),
      DataTable(VOICE_FREE_EG, 0x60C,
        Seq(
          ("Free EG Trigger", ONE, Interval(0x00, 0x03)),
          ("Free EG Loop Type", ONE, Interval(0x00, 0x04)),
          ("Free EG Length", ONE, Interval(0x02, 0x60)),
          ("Free EG Keyboard Track", ONE, anySeven)
        ) ++ trackParams ++ trackDatas
      ),
      DataTable(VOICE_STEP_SEQ, 0x66,
        Seq(
          ("Step Seq Base Unit", ONE, Discrete(Set(0x04, 0x06, 0x07))),
          ("Step Seq Length", ONE, Discrete(Set(0x08, 0x0C, 0x10))),
          ("RESERVED 1", ONE, anySeven),
          ("RESERVED 2", ONE, anySeven),
          ("RESERVED 3", ONE, anySeven),
          ("RESERVED 4", ONE, anySeven)
        ) ++
        sixteen("Step Seq Note", ONE, anySeven) ++
        sixteen("Step Seq Velocity", ONE, anySeven) ++
        sixteen("Step Seq Gate Time", LSB, anySeven) ++
        sixteen("Step Seq Control Change", ONE, anySeven) ++
        sixteen("Step Seq Gate Time", MSB, anySeven) ++
        sixteen("Step Seq Mute", ONE, onOff)
      ),
      DataTable(EFFECT, 0x03,
        Seq(
          // See effect lookup table
          ("Effect Type", MSB, Discrete(Set(0x00, 0x01, 0x02, 0x03))),
          ("Effect Type", LSB, Discrete(Set(0x00, 0x01, 0x02, 0x03))),
          ("Effect Parameter", ONE, anySeven)
        )
      ),
      DataTable(PART_MIX, 0x0F,
        Seq(
          reserved(1),
          reserved(2),
          reserved(3),
          reserved(4),
          reserved(5),
          ("Volume", ONE, anySeven),
          ("Pan", ONE, anySeven),
          ("Effect 1 Send", ONE, anySeven),
          reserved(6),
          reserved(7),
          ("Filter Cutoff Frequency", ONE, anySeven),
          ("Filter Resonance", ONE, anySeven),
          reserved(8),
          reserved(9),
          reserved(10)
        )
      ),
      DataTable(RHYTHM_STEP_SEQ, 0x66,
        Seq(
          reserved(1),
          reserved(2),
          reserved(3),
          reserved(4),
          reserved(5),
          reserved(6)
        ) ++
        sixteen("Step Seq Instrument", ONE, Interval(0x00, 0x78)) ++
        sixteen("Step Seq Velocity", ONE, anySeven) ++
        sixteen("Step Seq Gate Time", LSB, anySeven) ++
        sixteen("Step Seq Pitch", ONE, anySeven) ++
        sixteen("Step Seq Gate Time", MSB, Interval(0x00, 0x07)) ++
        sixteen("Step Seq Mute", ONE, onOff)
      ),
      DataTable(SONG_DATA, 0x0B,
        Seq(
          ("Pattern Num", MSB, anySeven),
          ("Pattern Num", LSB, anySeven),
          ("BPM", MSB, anySeven),
          ("BPM", LSB, anySeven),
          ("Play FX Gate Time", MSB, anySeven),
          ("Play FX Gate Time", LSB, anySeven),
          ("Beat", ONE, Discrete(Set(0x00, 0x01, 0x02, 0x03, 0x7F))),
          ("Swing", ONE, Union(Interval(0x32, 0x53), Discrete(Set(0x7F)))),
          ("Pitch", ONE, Union(Interval(0x28, 0x58), Discrete(Set(0x7F)))),
          ("Loop Type", ONE, Discrete(Set(0x00, 0x01, 0x7F))),
          ("Track Mute", ONE, Union(Interval(0x00, 0x0F), Discrete(Set(0x7F))))
        )
      )
    )

    private[this] def trackParams: Seq[DataRow] =
      (for {
        i <- 1 until (4 + 1)
      } yield {
        Seq(
          ("Free EG Track Param "+i, ONE, Interval(0x00, 0x1F)),
          ("Free EG Track Scene Switch "+i, ONE, onOff)
        )
      }).flatten

    private[this] def trackDatas: Seq[DataRow] =
      (for {
        i <- 1 until (4 + 1)
      } yield {
        (for {
          j <- 1 until (192 + 1)
        } yield {
          Seq(
            ("Free EG Track "+i+" Data "+j, MSB, topBit),
            ("Free EG Track "+i+" Data "+j, LSB, anySeven)
          )
        }).flatten
      }).flatten

    private[this] def sixteen(name: String, rel: RelType, range: ByteRange): Seq[DataRow] =
      for {
        i <- 1 until (16 + 1)
      } yield {
        (name+" "+i, ONE, range)
      }

    private[this] lazy val onOff: ByteRange = Discrete(Set(0x00, 0x01))
    private[this] lazy val topBit: ByteRange = onOff
    private[this] lazy val anySeven: ByteRange = Interval(0x00, 0x7F)

    private[this] def reserved(i: Int): DataRow =
      ("RESERVED "+i, ONE, anySeven)
  }

  case class Address(modelId: Byte, high: Byte, mid: Byte, low: Byte)

  case class Message(address: Address, data: SubFrame) {
    def count: SubFrame = throw TodoException
    def checksum: SubFrame = throw TodoException
  }

  case class PSeq(frameType: FrameType, parts: Map[SubFrameType, SubFrame]) {
    def toFrame: Frame = throw TodoException
    def message: Try[Message] = throw TodoException
  }


  def main(args: Array[String]) {
    println("running")
  }
}