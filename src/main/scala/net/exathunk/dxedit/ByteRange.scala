package net.exathunk.dxedit

trait ByteRange {
  def contains(byte: Byte): Boolean
  def size: Int
}

object ByteRange {
  case class Interval(start: Byte, end: Byte) extends ByteRange {
    override def contains(byte: Byte): Boolean =
      byte >= start && byte <= end
    override def size: Int = (end - start).toInt
  }

  case class Discrete(set: Set[Byte]) extends ByteRange {
    override def contains(byte: Byte): Boolean = set.contains(byte)
    override def size: Int = set.size
  }

  case class Union(a: ByteRange, b: ByteRange) extends ByteRange {
    override def contains(byte: Byte): Boolean =
      a.contains(byte) || b.contains(byte)
    override def size: Int = a.size + b.size
  }
}