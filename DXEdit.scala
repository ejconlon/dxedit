
object DXEdit {

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

  object TagType extends Enumeration {
    type TagType = Value
    val PATTERN = Value
    val PART = Value
    val SONG = Value
    val MEASURE = Value
  }

  object TableType extends Enumeration {
    type TableType = Value
    val VOICE_COMMON_1 = Value
    val VOICE_COMMON_2 = Value
    val VOICE_SCENE = Value
    val VOICE_FREE_EG = Value
    val VOICE_STEP_SEQ = Value
    val EFFECT = Value
    val PART_MIX = Value
    val RHYTHM_STEP_SEQ = Value
    val SONG = Value
  }

  object Constants {
    val START_TAG: Byte = 0xF7.toByte
  }

  import FrameType._
  import SubFrameType._
  import CountType._
  import RepeatType._
  import RelType._
  import TagType._
  import TableType._

  case class LookupTable[N, Row](name: N, size: Int, rows: Seq[Row])

  type ByteMatcher = Byte => Try[Byte]
  type FrameMatcher = Frame => Try[PSeq]
  type FrameRow = (SubFrameType, RepeatType, ByteMatcher)
  type FrameTable = LookupTable[FrameType, FrameRow]
  val FrameTable = LookupTable

  object ByteMatchers {
    def matchEquals(byte: Byte): ByteMatcher = throw TodoException
    def matchLike(pattern: String): ByteMatcher = throw TodoException
    def matchSeven: ByteMatcher = throw TodoException
    def matchOneOf(a: ByteMatcher, b: ByteMatcher): ByteMatcher = throw TodoException
  }

  object Messages {
    import ByteMatchers._
    import Constants._

    lazy val t1: FrameTable =
      FrameTable(
        DX200_NATIVE_BULK_DUMP, 12,
        Seq(
          (SYSEX_START, ONCE, matchEquals(START_TAG))
        ))
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
