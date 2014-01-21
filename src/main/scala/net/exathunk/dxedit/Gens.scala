package net.exathunk.dxedit

import org.scalacheck.Gen

object Gens {

  import AnnoType._
  import SubFrameType._

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

  def genParsedItem(row: DataRow): Gen[(String, Int)] = {
    row._3.gen map { x => (row._1, x) }
  }

  def genParsed(dataTable: DataTable): Gen[Map[String, Int]] = {
    val s: Gen[List[(String, Int)]] = Gen.sequence[List, (String, Int)](dataTable.rows map genParsedItem)
    s map { _.toSeq.toMap }
  }

  def genAnno(dataTable: DataTable): Gen[Map[AnnoType, Int]] = {
    val pattern: Gen[(AnnoType, Int)] = Gen.chooseNum(0, 127) map { x => (AnnoType.PATTERN, x) }
    val part: Gen[(AnnoType, Int)] = Gen.chooseNum(0, 3) map { x => (AnnoType.PART, x) }
    val song: Gen[(AnnoType, Int)] = Gen.chooseNum(0, 127) map { x => (AnnoType.SONG, x) }
    val measure: Gen[(AnnoType, Int)] = Gen.chooseNum(0, 0x3FFF) map { x => (AnnoType.MEASURE, x) }
    def toMap(xs: Gen[(AnnoType, Int)]*): Gen[Map[AnnoType, Int]] = {
      Gen.sequence[List, (AnnoType, Int)](xs) map { _.toSeq.toMap }
    }
    dataTable.name match {
      case DataType.VOICE_COMMON_1 => toMap(pattern)
      case DataType.VOICE_COMMON_2 => toMap(pattern)
      case DataType.VOICE_SCENE => toMap(pattern)
      case DataType.VOICE_FREE_EG => toMap(pattern)
      case DataType.VOICE_STEP_SEQ => toMap(pattern)
      case DataType.RHYTHM_STEP_SEQ => toMap(pattern, part)
      case DataType.EFFECT => toMap(pattern)
      case DataType.PART_MIX => toMap(part, pattern)
      case DataType.SONG_DATA => toMap(song, measure)
    }
  }

  def genAnnoData: Gen[AnnoData] = {
    for {
      //dataTable <- Gen.oneOf(SecondPass.tables)  // TODO
      dataTable <- Gen.oneOf(SecondPass.tableMap(DataType.VOICE_COMMON_1), SecondPass.tableMap(DataType.VOICE_COMMON_2))
      anno <- genAnno(dataTable)
      parsed <- genParsed(dataTable)
    } yield {
      AnnoData(parsed, AnnoTable(anno, dataTable))
    }
  }

}