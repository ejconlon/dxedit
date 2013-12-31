package net.exathunk.dxedit

import scala.util.Try

object DXEdit {

  import FrameType._
  import SubFrameType._

  def extractFrames(bytes: Seq[Byte]): Try[Seq[Frame]] = throw TodoException

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
