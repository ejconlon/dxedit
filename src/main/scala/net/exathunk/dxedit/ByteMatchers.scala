package net.exathunk.dxedit

object ByteMatchers {
  import ImplicitIntToByte._

  private[this] def mkbm(x: Byte => Option[Byte]): ByteMatcher = ByteMatcher(x, x)

  def matchAny: ByteMatcher = mkbm { x: Byte => Some(x) }
  def matchEquals(byte: Byte): ByteMatcher =
    mkbm { x: Byte => if (x == byte) Some(byte) else None }

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

    val forward = { x: Byte =>
      val matches: Seq[Boolean] = s map { t =>
        val isNonzero = (t._1 & x) != 0
        isNonzero == t._2
      }
      if (matches forall { x => x }) Some[Byte](retMask & x) else None
    }

    val backward = { x: Byte =>
      if ((~retMask & x) != 0) {
        None
      } else {
        var y = x
        s foreach { bb =>
          if (bb._2) {
            y = y | bb._1
          } else {
            y = y & (~bb._1)
          }
        }
        Some(y)
      }
    }

    ByteMatcher(forward, backward)
  }
  def matchSeven: ByteMatcher =
    mkbm { x => if ((x & 0x80) == 0) Some(x) else None }

  def matchOneOf(a: ByteMatcher, b: ByteMatcher): ByteMatcher = ByteMatcher(
    { x =>
      val y = a.forward(x)
      if (y.isDefined) y else b.forward(x)
    },
    { x =>
      val y = a.backward(x)
      if (y.isDefined) y else b.backward(x)
    }
  )

}