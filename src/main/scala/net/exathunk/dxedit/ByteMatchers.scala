package net.exathunk.dxedit

object ByteMatchers {
  def matchEquals(byte: Byte): ByteMatcher = throw TodoException
  def matchLike(pattern: String): ByteMatcher = throw TodoException
  def matchSeven: ByteMatcher = throw TodoException
  def matchOneOf(a: ByteMatcher, b: ByteMatcher): ByteMatcher = throw TodoException
}