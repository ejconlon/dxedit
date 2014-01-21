package net.exathunk.dxedit

import org.scalacheck.Gen

trait ByteRange {
  def contains(byte: Byte): Boolean
  def size: Int
  def gen: Gen[Byte]
}

object ByteRange {
  case class Interval(start: Byte, end: Byte) extends ByteRange {
    override def contains(byte: Byte): Boolean =
      byte >= start && byte <= end
    override def size: Int = (end - start).toInt
    override def gen: Gen[Byte] = Gen.chooseNum(start, end)
  }

  case class Discrete(set: Set[Byte]) extends ByteRange {
    override def contains(byte: Byte): Boolean = set.contains(byte)
    override def size: Int = set.size
    override def gen: Gen[Byte] = Gen.oneOf(set toSeq)
  }

  case class Union(a: ByteRange, b: ByteRange) extends ByteRange {
    override def contains(byte: Byte): Boolean =
      a.contains(byte) || b.contains(byte)
    override def size: Int = a.size + b.size
    override def gen: Gen[Byte] = Gen.oneOf(a, b) flatMap { x => x.gen }
  }
}