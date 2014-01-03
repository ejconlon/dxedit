package net.exathunk.dxedit

import scala.util.{Failure, Success, Try}
import scala.collection.SortedMap

object DXEdit {

  import FrameType._
  import SubFrameType._

  sealed trait ExtractException extends Exception
  case object MismatchException extends ExtractException
  case object OverlapException extends ExtractException
  case object ContiguityException extends ExtractException

  def extractFrames(bytes: Seq[Byte]): Try[Seq[Frame]] = {
    val starts = bytes.zipWithIndex.collect { case(Constants.START_TAG, i) => i }
    val ends = bytes.zipWithIndex.collect { case(Constants.END_TAG, i) => i }
    if (starts.size != ends.size) {
      Failure(MismatchException)
    } else {
      val ivals = starts.zip(ends)
      val b = Seq.newBuilder[Frame]
      var exc: Option[ExtractException] = None
      var lastIndex = -1
      ivals.foreach { ival =>
        if (ival._1 > ival._2) {
          exc = Some(OverlapException)
        } else if (ival._1 != lastIndex + 1) {
          exc = Some(ContiguityException)
        } else {
          b += bytes.slice(ival._1, ival._2 + 1)
          lastIndex = ival._2
        }
      }
      if (exc.isDefined) {
        Failure(exc.get)
      } else if (lastIndex != bytes.size - 1) {
        Failure(ContiguityException)
      } else {
        Success(b.result)
      }
    }
  }

  object Pass {
    def seq[A, B, C](p: Pass[A, B], q: Pass[B, C]): Pass[A, C] =
      new Pass[A, C] {
        override def runPass(a: A): Try[C] = {
          p.runPass(a) flatMap { b => q.runPass(b) }
        }
        override def validate = { p.validate; q.validate }
      }
  }

  trait Pass[A, B] {
    def runPass(a: A): Try[B]
    def validate: Unit
  }

  case class Address(modelId: Byte, high: Byte, mid: Byte, low: Byte)

  case class Message(address: Address, data: SubFrame) {
    lazy val count: (Byte, Byte) = splitBytes(data.size)
    // TODO test
    lazy val checksum: Byte = {
      import ImplicitIntToByte._
      var s: Int = 1
      s += address.high
      s += address.mid
      s += address.low
      s += count._1
      s += count._2
      data.foreach { s += _ }
      0xFF ^ s & 0x7F
    }
  }

  case class PSeq(frameType: FrameType, parts: SortedMap[SubFrameType, SubFrame]) {
    // TODO test
    def toFrame: Frame = {
      val s = Seq.newBuilder[Byte]
      parts.values foreach { v => s ++= v }
      s.result
    }

    def message: Option[Message] =
      for {
        modelId <- parts.get(MODEL_ID) flatMap singleton
        high <- parts.get(ADDR_HIGH) flatMap singleton
        mid <- parts.get(ADDR_MID) flatMap singleton
        low <- parts.get(ADDR_LOW) flatMap singleton
        data <- parts.get(DATA)
      } yield {
        Message(Address(modelId, high, mid, low), data)
      }

    // TODO test
    def replace(message: Message): PSeq = {
      val m = SortedMap.newBuilder[SubFrameType, SubFrame]
      m ++= parts
      m += (MODEL_ID -> Seq(message.address.modelId))
      m += (ADDR_HIGH -> Seq(message.address.high))
      m += (ADDR_MID -> Seq(message.address.mid))
      m += (ADDR_LOW -> Seq(message.address.low))
      m += (BYTE_COUNT_MSB -> Seq(message.count._1))
      m += (BYTE_COUNT_LSB -> Seq(message.count._2))
      m += (CHECKSUM -> Seq(message.checksum))
      m += (DATA -> message.data)
      PSeq(frameType, m.result)
    }
  }

  case class AnnoData

  def main(args: Array[String]) {
    println("running")
  }
}
