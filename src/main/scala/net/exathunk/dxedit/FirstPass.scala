package net.exathunk.dxedit

import scala.util.control.Breaks._
import scala.util.{Failure, Success, Try}

object FirstPass extends Pass[Frame, PSeq] {
  import ByteMatchers._
  import Constants._
  import FrameType._
  import RepeatType._
  import SubFrameType._

  override def runPass(frame: Frame): Try[PSeq] = {
    val excs = Seq.newBuilder[SubFrameMismatchException]
    tables foreach { table =>
      val t = runPassWith(table, frame)
      if (t.isFailure) {
        excs += t.failed.get.asInstanceOf[SubFrameMismatchException]
      } else {
        return t
      }
    }
    Failure(MultiSubFrameMismatchException(excs.result))
  }

  override def unRunPass(pseq: PSeq): Try[Frame] = {
    pseq.toFrame
  }

  def runPassWith(frameTable: FrameTable, frame: Frame): Try[PSeq] = {
    val rs = Seq.newBuilder[(SubFrameType, SubFrame)]
    val spec = frameTable.rows
    var frameI = 0
    var specI = 0
    breakable {
      while (specI < spec.size) {
        val section = spec(specI)
        if (section._2 == RepeatType.MANY) {
          break
        } else {
          val byte: Byte = frame(frameI)
          val matcher: ByteMatcher = section._3
          val r: Option[Byte] = matcher.forward(byte)
          r match {
            case None => return Failure(SubFrameMismatchException(section._1, frameI, byte))
            case Some(b) => rs += ((section._1, Seq(b)))
          }
          frameI += 1
          specI += 1
        }
      }
    }
    // if we broke early for a many section
    if (specI < spec.size) {
      val revRs = Seq.newBuilder[(SubFrameType, SubFrame)]
      var frameJ = frame.size - 1
      var specJ = spec.size - 1
      while (specJ > specI) {
        val section = spec(specJ)
        assert(section._2 == RepeatType.ONCE)
        val byte: Byte = frame(frameJ)
        val matcher: ByteMatcher = section._3
        val r: Option[Byte] = matcher.forward(byte)
        r match {
          case None => return Failure(SubFrameMismatchException(section._1, frameJ, byte))
          case Some(b) => revRs += ((section._1, Seq(b)))
        }
        frameJ -= 1
        specJ -= 1
      }
      // now handle the many section
      assert(specJ == specI)
      val section = spec(specI)
      assert(section._2 == RepeatType.MANY)
      val ds = Seq.newBuilder[Byte]
      while (frameI <= frameJ) {
        val byte: Byte = frame(frameI)
        val matcher: ByteMatcher = section._3
        val r: Option[Byte] = matcher.forward(byte)
        r match {
          case None => return Failure(SubFrameMismatchException(section._1, frameI, byte))
          case Some(b) => ds += b
        }
        frameI += 1
      }
      rs += ((section._1, ds.result))
      rs ++= revRs.result.reverse
    }

    val m: Map[SubFrameType, SubFrame] = rs.result.toMap
    Success(PSeq(frameTable, m))
  }

  override def validate: Unit =
    for {
      table <- tables
    } {
      val numMany: Int = (table.rows.filter { _._2 == RepeatType.MANY } size)
      if (numMany > 1) throw new Exception("More than one MANY field: " + table.name)
      table.validate
    }

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

  lazy val tableMap: Map[FrameType, FrameTable] =
    (for { t <- tables } yield { (t.name, t) }).toMap
}