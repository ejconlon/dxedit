package net.exathunk.dxedit

object FirstPass {
  import ByteMatchers._
  import Constants._
  import net.exathunk.dxedit.Constants
  import net.exathunk.dxedit.Constants._
  import net.exathunk.dxedit.FrameType._
  import net.exathunk.dxedit.RepeatType._
  import net.exathunk.dxedit.SubFrameType._


  type FrameRow = (SubFrameType, RepeatType, ByteMatcher)
  type FrameTable = LookupTable[FrameType, FrameRow]
  val FrameTable = LookupTable

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