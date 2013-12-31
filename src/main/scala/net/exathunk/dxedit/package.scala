package net.exathunk

package object dxedit {

  type Frame = Seq[Byte]
  type SubFrame = Seq[Byte]
  type Data = Seq[Byte]
  type ByteMatcher = Byte => Option[Byte]

  case class LookupTable[N, Row](name: N, size: Int, rows: Seq[Row])

  case object TodoException extends RuntimeException

  // Import into SMALL SCOPES to avoid toByte'ing all the literal bytes there
  object ImplicitIntToByte {
    implicit def intToByte(int: Int): Byte = int.toByte
  }

}

