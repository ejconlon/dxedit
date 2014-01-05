package net.exathunk.dxedit

case class Address(modelId: Byte, high: Byte, mid: Byte, low: Byte)

case class Message(address: Address, data: SubFrame) {
  lazy val count: (Byte, Byte) = splitBytes(data.size)
  // TODO test
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

case class PSeq(frameTable: FrameTable, parts: Map[SubFrameType, SubFrame]) {
  def toFrame: Option[Frame] = {
    val s = Seq.newBuilder[Byte]
    frameTable.rows foreach { row =>
      val part = parts.get(row._1)
      if (part.isEmpty || part.get.isEmpty) return None
      else if (row._2 == RepeatType.ONCE && part.get.size > 1) return None
      part.get.foreach { p =>
        val matched = row._3.backward(p)
        if (matched.isEmpty) return None
        else s += matched.get
      }
    }
    Some(s.result)
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
