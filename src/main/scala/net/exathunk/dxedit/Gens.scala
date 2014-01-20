package net.exathunk.dxedit

import org.scalacheck.Gen

object Gens {

  def genPSeq: Gen[PSeq] = {
    for {
      frameTable <- Gen.oneOf(FirstPass.tables)
    } yield {
      PSeq(frameTable, Map())
    }
  }



}