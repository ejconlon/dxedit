package net.exathunk.dxedit

object ByteMatchers {
  import ImplicitIntToByte._

  def matchAny: ByteMatcher = { x => Some(x) }
  def matchEquals(byte: Byte): ByteMatcher = { x => if (x == byte) Some(byte) else None }
  def matchLike(pattern: String): ByteMatcher = {
    if (pattern.size != 8) throw new Exception("Invalid pattern")
    val m = Seq.newBuilder[(Byte, Boolean)]
    var retMask = 0
    for {
      i <- (0 until 8)
    } {
      val j = 7-i
      val mask = 1 << i
      val c = pattern.charAt(j)
      if (c == '0') {
        m += ((mask, false))
      } else if (c == '1') {
        m += ((mask, true))
      } else {
        retMask |= mask
      }
    }
    val s: Seq[(Byte, Boolean)] = m.result()

    { x: Byte =>
      val matches: Seq[Boolean] = s map { t =>
        val isNonzero = (t._1 & x) != 0
        isNonzero == t._2
      }
      if (matches forall { x => x }) Some(retMask & x) else None
    }
  }
  def matchSeven: ByteMatcher = { x => if ((x & 0x80) == 0) Some(x) else None }
  def matchOneOf(a: ByteMatcher, b: ByteMatcher): ByteMatcher = { x =>
    val y = a(x)
    if (y.isDefined) y else b(x)
  }
}