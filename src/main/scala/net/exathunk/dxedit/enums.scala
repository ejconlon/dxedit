package net.exathunk.dxedit

object FrameType extends Enumeration {
  type FrameType = Value
  // These order message parsing so be careful
  val DX200_NATIVE_BULK_DUMP = Value
  val DX_BULK_DUMP = Value
  val DX200_NATIVE_PARAM_CHANGE = Value
  val DX_PARAM_CHANGE = Value
}

// NOTE: The ordering of this table is important to PSeq.toFrame
object SubFrameType extends Enumeration {
  type SubFrameType = Value
  val SYSEX_START = Value
  val SYSEX_END = Value
  val MFR_ID = Value
  val DEVICE_NUM = Value
  val MODEL_ID = Value
  val PARAMETER_GROUP_NUM = Value
  val PARAMETER_NUM = Value
  val FORMAT_NUM = Value
  val BYTE_COUNT_MSB = Value
  val BYTE_COUNT_LSB = Value
  val ADDR_HIGH = Value
  val ADDR_MID = Value
  val ADDR_LOW = Value
  val DATA = Value
  val CHECKSUM = Value
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