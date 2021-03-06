package net.exathunk

import org.scalacheck.Gen
import scala.util.Try

package object dxedit {

  import FrameType._
  import RepeatType._
  import SubFrameType._
  import RelType._
  import DataType._

  type Frame = Seq[Byte]
  type SubFrame = Seq[Byte]
  type Data = Seq[Byte]
  case class ByteMatcher(
    forward: Byte => Option[Byte],
    backward: Byte => Option[Byte],
    gen: Gen[Byte]
  )

  type FrameRow = (SubFrameType, RepeatType, ByteMatcher)
  type FrameTable = LookupTable[FrameType, FrameRow]
  val FrameTable = LookupTable

  type DataRow = (String, RelType, ByteRange)
  type DataTable = LookupTable[DataType, DataRow]
  val DataTable = LookupTable

  case class AnnoTable(anno: Map[AnnoType.Value, Int], table: DataTable)
  case class AnnoData(data: Map[String, Int], annoTable: AnnoTable)

  object Pass {
    def seq[A, B, C](p: Pass[A, B], q: Pass[B, C]): Pass[A, C] =
      new Pass[A, C] {
        override def runPass(a: A): Try[C] = {
          p.runPass(a) flatMap { b => q.runPass(b) }
        }
        override def unRunPass(c: C): Try[A] = {
          q.unRunPass(c) flatMap { b => p.unRunPass(b) }
        }
        override def validate = { p.validate; q.validate }
      }
  }

  trait Pass[A, B] {
    def runPass(a: A): Try[B]
    def unRunPass(b: B): Try[A]
    def validate: Unit
  }

  case class MultiSubFrameMismatchException(mismatches: Seq[SubFrameMismatchException]) extends RuntimeException
  case class SubFrameMismatchException(subFrameType: SubFrameType, index: Int, byte: Byte) extends RuntimeException

  case class LookupTable[N, Row](name: N, size: Int, rows: Seq[Row]) {
    def validate = assert(size == rows.size)
  }

  case object TodoException extends RuntimeException

  // Import into SMALL SCOPES to avoid toByte'ing all the literal bytes there
  object ImplicitIntToByte {
    implicit def intToByte(int: Int): Byte = int.toByte
  }

  def singleton[T](xs: Seq[T]): Option[T] = if (xs.size == 1) Some(xs(0)) else None

  def splitBytes(count: Int): (Byte, Byte) = {
    assert(count < (1 << 14))
    ((count >> 7).toByte, (count & 0x7F).toByte)
  }

  def joinBytes(msb: Byte, lsb: Byte): Byte = {
    ((msb << 7) | lsb).toByte
  }
}

