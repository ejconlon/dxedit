package net.exathunk.dxedit

import scala.util.{Failure, Success, Try}

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

  trait FrameReader {
    def readPSeq(frame: Frame): Try[PSeq]
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
