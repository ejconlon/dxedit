package net.exathunk.dxedit

import org.scalacheck.Gen

object Gens {

  import SubFrameType._
  import Constants._

  def genSubFrame(row: FrameRow): Gen[(SubFrameType, SubFrame)] = {
    val g: Gen[Byte] = row._3.gen map { x => row._3.forward(x).get }
    if (row._2 == RepeatType.ONCE) {
      Gen.listOfN(1, g) map { x => (row._1, x) }
    } else {
      Gen.chooseNum(1, 127) flatMap { n =>
        Gen.listOfN(n, g) map { x => (row._1, x) }
      }
    }
  }

  def genParts(frameTable: FrameTable): Gen[Map[SubFrameType, SubFrame]] = {
    val s: Gen[List[(SubFrameType, SubFrame)]] = Gen.sequence[List, (SubFrameType, SubFrame)](frameTable.rows map genSubFrame)
    s map { _.toSeq.toMap }
  }

  def genPSeq: Gen[PSeq] = {
    for {
      frameTable <- Gen.oneOf(FirstPass.tables)
      parts <- genParts(frameTable)
    } yield {
      PSeq(frameTable, parts)
    }
  }

}