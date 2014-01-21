package net.exathunk.dxedit

case class Address(modelId: Byte, high: Byte, mid: Byte, low: Byte)

case class Message(address: Address, data: SubFrame) {
  lazy val count: (Byte, Byte) = splitBytes(data.size)

  lazy val checksum: Byte = {
    import ImplicitIntToByte._
    var s: Int = 0
    s += address.high
    s += address.mid
    s += address.low
    s += count._1
    s += count._2
    data.foreach { s += _ }
    ((0xFF ^ s) + 1) & 0x7F
  }
}

import SubFrameType._
import scala.util.Try
import scala.util.Success
import scala.util.Failure

case class PSeq(frameTable: FrameTable, parts: Map[SubFrameType, SubFrame]) {
  def toFrame: Try[Frame] = {
    val s = Seq.newBuilder[Byte]
    frameTable.rows foreach { row =>
      val name = row._1
      val part = parts.get(name)
      if (part.isEmpty || part.get.isEmpty) return Failure(new Exception("Missing part: " + name))
      else if (row._2 == RepeatType.ONCE && part.get.size > 1) return Failure(new Exception("Multi part: " + name))
      part.get.foreach { p =>
        val matched = row._3.backward(p)
        if (matched.isEmpty) return Failure(new Exception("Mismatched part: " + name))
        else s += matched.get
      }
    }
    Success(s.result)
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

  def replace(message: Message): PSeq = {
    val m = Map.newBuilder[SubFrameType, SubFrame]
    m ++= parts
    m += (MODEL_ID -> Seq(message.address.modelId))
    m += (ADDR_HIGH -> Seq(message.address.high))
    m += (ADDR_MID -> Seq(message.address.mid))
    m += (ADDR_LOW -> Seq(message.address.low))
    m += (BYTE_COUNT_MSB -> Seq(message.count._1))
    m += (BYTE_COUNT_LSB -> Seq(message.count._2))
    m += (CHECKSUM -> Seq(message.checksum))
    m += (DATA -> message.data)
    PSeq(frameTable, m.result)
  }
}
