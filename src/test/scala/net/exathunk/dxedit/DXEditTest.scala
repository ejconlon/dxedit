package net.exathunk.dxedit

import org.scalatest.FunSuite
import scala.collection.SortedMap
import scala.util.{Failure, Success}

class DXEditTest extends FunSuite {

  // Matcher tests

  import ImplicitIntToByte._
  import ByteMatchers._

  def assertMatchEquals(gold: Byte, test: Byte) {
    assert(Some(gold) == matchEquals(gold)(test))
  }
  def assertNotMatchEquals(gold: Byte, test: Byte) {
    assert(None == matchEquals(gold)(test))
  }
  def assertMatchAny(test: Byte) {
    assert(Some(test) == matchAny(test))
  }
  def assertMatchLike(gold: Byte, pattern: String, test: Byte) {
    assert(Some(gold) == matchLike(pattern)(test))
  }
  def assertNotMatchLike(pattern: String, test: Byte) {
    assert(None == matchLike(pattern)(test))
  }
  def assertMatchSeven(test: Byte) {
    assert(Some(test) == matchSeven(test))
  }
  def assertNotMatchSeven(test: Byte) {
    assert(None == matchSeven(test))
  }

  test("matchEquals") {
    assertMatchEquals(3, 3)
    assertNotMatchEquals(3, 4)
  }

  test("matchAny") {
    assertMatchAny(3)
  }

  test("matchLike") {
    assertMatchLike(0xF, "1111vvvv", 0xFF)
    assertNotMatchLike("1111vvvv", 0xF)
    assertMatchLike(0xF, "0000vvvv", 0xF)
    assertNotMatchLike("0000vvvv", 0xFF)
    assertMatchLike(0xF0, "vvvv0000", 0xF0)
    assertNotMatchLike("vvvv0000", 0xFF)
    assertMatchLike(0xF0, "vvvv1111", 0xFF)
    assertNotMatchLike("vvvv1111", 0xF0)
  }

  test("matchSeven") {
    assertMatchSeven(0x7F)
    assertNotMatchSeven(0xFF)
  }

  test("matchOneOf") {
    val m = matchOneOf(matchEquals(0xF), matchEquals(0x0))
    assert(Some(0xF) == m(0xF))
    assert(Some(0x0) == m(0x0))
    assert(None == m(0x9))
  }

  val dxParamChangeSeq: Seq[Byte] = Seq(
    0xF0, 0x43, 0x10, 0x19, 0x4D, 0x00, 0xF7)

  val dx200NativeBulkDumpSeq: Seq[Byte] = Seq(
    0xF0, 0x43, 0x00, 0x62, 0x00, 0x05, 0x21, 0x7F,
    0x00, 0x03, 0x00, 0x01, 0x0C, 0x32, 0x19, 0xF7)

  test("extractFrames") {
    // Extract multiple contiguous frames
    val twoFrames = dx200NativeBulkDumpSeq ++ dx200NativeBulkDumpSeq
    assert(Success(Seq(dx200NativeBulkDumpSeq, dx200NativeBulkDumpSeq)) == DXEdit.extractFrames(twoFrames))

    // Fail to extract incomplete frame
    val incomplete = dx200NativeBulkDumpSeq.slice(0, dx200NativeBulkDumpSeq.size - 1)
    assert(Failure(DXEdit.MismatchException) == DXEdit.extractFrames(incomplete))

    // Fail to extract non-contiguous frames
    val nonContig = dx200NativeBulkDumpSeq ++ Seq[Byte](0xAB) ++ dx200NativeBulkDumpSeq
    assert(Failure(DXEdit.ContiguityException) == DXEdit.extractFrames(nonContig))

    val leading = Seq[Byte](0xAB) ++ dx200NativeBulkDumpSeq
    assert(Failure(DXEdit.ContiguityException) == DXEdit.extractFrames(leading))

    val trailing = dx200NativeBulkDumpSeq ++ Seq[Byte](0xAB)
    assert(Failure(DXEdit.ContiguityException) == DXEdit.extractFrames(trailing))
  }

  test("validate passes") {
    FirstPass.validate
    SecondPass.validate
  }

  test("FirstPass no many parse") {
    val expected: DXEdit.PSeq = DXEdit.PSeq(FrameType.DX_PARAM_CHANGE, SortedMap(
      (SubFrameType.SYSEX_START -> Seq(0xF0)),
      (SubFrameType.MFR_ID -> Seq(0x43)),
      (SubFrameType.DEVICE_NUM -> Seq(0x00)), // match out a leading 0001
      (SubFrameType.PARAMETER_GROUP_NUM -> Seq(0x19)),
      (SubFrameType.PARAMETER_NUM -> Seq(0x4D)),
      (SubFrameType.DATA -> Seq(0x00)),
      (SubFrameType.SYSEX_END -> Seq(0xF7))
    ))
    val frameTable = FirstPass.tableMap(FrameType.DX_PARAM_CHANGE)
    val actualSuccess = FirstPass.runPassWith(frameTable, dxParamChangeSeq)
    assert(Success(expected) == actualSuccess)

    val actualFailure = FirstPass.runPassWith(frameTable, dx200NativeBulkDumpSeq)
    assert(Failure(FirstPass.MismatchException(SubFrameType.DEVICE_NUM, 2, 0x00)) == actualFailure)
  }

  test("FirstPass many parse") {
    val expected: DXEdit.PSeq = DXEdit.PSeq(FrameType.DX200_NATIVE_BULK_DUMP, SortedMap(
      (SubFrameType.SYSEX_START, Seq(0xF0)),
      (SubFrameType.MFR_ID, Seq(0x43)),
      (SubFrameType.DEVICE_NUM, Seq(0x00)),
      (SubFrameType.MODEL_ID, Seq(0x62)),
      (SubFrameType.BYTE_COUNT_MSB, Seq(0x00)),
      (SubFrameType.BYTE_COUNT_LSB, Seq(0x05)),
      (SubFrameType.ADDR_HIGH, Seq(0x21)),
      (SubFrameType.ADDR_MID, Seq(0x7F)),
      (SubFrameType.ADDR_LOW, Seq(0x00)),
      (SubFrameType.DATA, Seq(0x03, 0x00, 0x01, 0x0C, 0x32)),
      (SubFrameType.CHECKSUM, Seq(0x19)),
      (SubFrameType.SYSEX_END, Seq(0xF7))
    ))
    val frameTable = FirstPass.tableMap(FrameType.DX200_NATIVE_BULK_DUMP)
    val actualSuccess = FirstPass.runPassWith(frameTable, dx200NativeBulkDumpSeq)
    assert(Success(expected) == actualSuccess)

    val actualFailure = FirstPass.runPassWith(frameTable, dxParamChangeSeq)
    assert(Failure(FirstPass.MismatchException(SubFrameType.DEVICE_NUM, 2, 0x10)) == actualFailure)
  }

  test("FirstPass either") {
    assert(FirstPass.runPass(dx200NativeBulkDumpSeq).isSuccess)
    assert(FirstPass.runPass(dxParamChangeSeq).isSuccess)
  }
}
