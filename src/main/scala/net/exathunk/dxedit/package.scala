package net.exathunk

package object dxedit {

  type Frame = Seq[Byte]
  type SubFrame = Seq[Byte]
  type Data = Seq[Byte]
  type ByteMatcher = Byte => Option[Byte]

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

