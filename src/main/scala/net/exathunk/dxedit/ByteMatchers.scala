package net.exathunk.dxedit

import org.scalacheck.Gen
import org.scalacheck.Arbitrary._

object ByteMatchers {
  import ImplicitIntToByte._

  private[this] def mkbm(x: Byte => Option[Byte], g: Gen[Byte]): ByteMatcher = ByteMatcher(x, x, g)

  def matchAny: ByteMatcher = mkbm({ x: Byte => Some(x) }, arbitrary[Byte])
  def matchEquals(byte: Byte): ByteMatcher =
    mkbm({ x: Byte => if (x == byte) Some(byte) else None }, Gen.value(byte))

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

    val gen: Gen[Byte] = arbitrary[Byte] flatMap { b =>
      var x: Byte = b
      s.foreach { bb =>
        if (bb._2) {
          x = x | bb._1
        } else {
          x = x & (~bb._1)
        }
      }
      x
    }

    ByteMatcher(forward, backward, gen)
  }
  def matchSeven: ByteMatcher =
    mkbm({ x => if ((x & 0x80) == 0) Some(x) else None }, Gen.chooseNum(0x00, 0x7F))

  def matchOneOf(a: ByteMatcher, b: ByteMatcher): ByteMatcher = ByteMatcher(
    { x =>
      val y = a.forward(x)
      if (y.isDefined) y else b.forward(x)
    },
    { x =>
      val y = a.backward(x)
      if (y.isDefined) y else b.backward(x)
    },
    Gen.oneOf(a, b) flatMap { x => x.gen }
  )

  def matchPosNeg(pos: ByteMatcher, neg: ByteMatcher): ByteMatcher = ByteMatcher(
    { x =>
      val z = neg.forward(x)
      if (z.isDefined) None else pos.forward(x)
    },
    { x =>
      val z = neg.backward(x)
      if (z.isDefined) None else pos.backward(x)
    },
    for {
      x <- pos.gen if neg.forward(x).isEmpty
    } yield x
  )

}