package net.exathunk.dxedit

import org.scalatest.FunSuite
import org.scalatest.prop.Checkers
import org.scalacheck.Prop
import org.scalacheck.Prop._

class GenTest extends FunSuite with Checkers {
  private[this] def checkMatcher(m: ByteMatcher): Prop =
    forAll(m.gen) { b =>
      val c = m.forward(b)
      val d = c flatMap { x => m.backward(x) }
      c.isDefined && d.isDefined && d == Some(b)
    }

  test("matchAny") {
    check {
      checkMatcher(ByteMatchers.matchAny)
    }
  }

  test("matchEquals") {
    check {
      checkMatcher(ByteMatchers.matchEquals(0x62))
    }
  }

  test("matchSeven") {
    check {
      checkMatcher(ByteMatchers.matchSeven)
    }
  }

  test("matchLike") {
    check {
      checkMatcher(ByteMatchers.matchLike("0001nnnn"))
    }
  }

  test("matchOneOf") {
    check {
      checkMatcher(ByteMatchers.matchOneOf(ByteMatchers.matchEquals(0x62), ByteMatchers.matchEquals(0x6D)))
    }
  }

  test("FirstPass") {
    check {
      forAll(Gens.genPSeq) { pseq =>
        val frame: Frame = FirstPass.unRunPass(pseq).get
        val back: PSeq = FirstPass.runPass(frame).get
        if (pseq != back) {
          println(pseq.toFrame)
          println(back.toFrame)
          println(pseq.parts)
          println(back.parts)
          println(pseq.frameTable.name)
          println(back.frameTable.name)
          println(pseq.toFrame == back.toFrame)
          println(pseq.frameTable == back.frameTable)
          println(pseq.parts == back.parts)
        }
        pseq == back
      }
    }
  }

  test("SecondPass") {
    check {
      forAll(Gens.genAnnoData) { annoData =>
        val pseq: PSeq = SecondPass.unRunPass(annoData).get
        val back: AnnoData = SecondPass.runPass(pseq).get
        annoData == back
      }
    }
  }
}
