package net.exathunk.dxedit

import scala.util.{Success, Failure, Try}

object SecondPass extends Pass[PSeq, AnnoData] {
  import ImplicitIntToByte._
  import ByteRange._

  import DataType._
  import RelType._

  override def runPass(pseq: PSeq): Try[AnnoData] = {
    val message = pseq.message
    if (message.isEmpty) return Failure(new Exception("No message"))
    val annoTable = message flatMap { m => getAnnoTable(m.address) }
    if (annoTable.isEmpty) return Failure(new Exception("No table"))
    parse(message.get, annoTable.get)
  }

  private[this] def parse(message: Message, annoTable: AnnoTable): Try[AnnoData] = {
    if (message.data.size != annoTable.table.rows.size) return Failure(new Exception("Size mismatch"))
    val m = scala.collection.mutable.Map[String, scala.collection.mutable.Map[RelType.Value, Byte]]()
    val pairs = annoTable.table.rows zip message.data
    pairs.foreach { p: (DataRow, Byte) =>
      val row = p._1
      val name = row._1
      val rel = row._2
      val range = row._3
      val byte = p._2
      if (!range.contains(byte)) {
        return Failure(new Exception("Not in range"))
      }
      if (!m.contains(name)) {
        m(name) = scala.collection.mutable.Map[RelType.Value, Byte]()
      }
      m(name)(rel) = byte
    }
    val ns = Map.newBuilder[String, Int]
    m.keys.foreach { k =>
      val vd = m(k)
      println(k +" -> "+vd)
      if (vd.size == 1) {
        assert(vd.contains(RelType.ONE))
        ns += (k -> vd(RelType.ONE))
      } else if (vd.size == 2) {
        assert(vd.contains(RelType.MSB))
        assert(vd.contains(RelType.LSB))
        val v = (vd(RelType.MSB).toInt << 7) | vd(RelType.LSB).toInt
        ns += (k -> v)
      } else {
        return Failure(new Exception("invalid num bytes"))
      }
    }
    Success(AnnoData(ns.result, annoTable))
  }

  override def validate = for { table <- tables } table.validate

  lazy val tables: Seq[DataTable] = Seq(
    DataTable(
      VOICE_COMMON_1, 0x29,
      Seq(
        ("Distortion: Off/On", ONE, onOff),
        ("Distortion: Drive", ONE, Interval(0x00, 0x64)),
        ("Distortion: AMP Type", ONE, Interval(0x00, 0x03)),
        ("Distortion: LPF Cutoff", ONE, Interval(0x22, 0x3C)),
        ("Distortion: Out Level", ONE, Interval(0x00, 0x64)),
        ("Distortion: Dry/Wet", ONE, Interval(0x01, 0x7F)),
        ("2-Band EQ Low Freq", ONE, Interval(0x04, 0x28)),
        ("2-Band EQ Low Gain", ONE, Interval(0x34, 0x4C)),
        ("2-Band EQ Mid Freq", ONE, Interval(0x0E, 0x36)),
        ("2-Band EQ Mid Gain", ONE, Interval(0x34, 0x4C)),
        ("2-Band EQ Mid Resonance(Q)", ONE, Interval(0x0A, 0x78)),
        ("RESERVED 1", ONE, anySeven),
        ("Filter Cutoff", ONE, anySeven),
        ("Filter Resonance(Q)", ONE, Interval(0x00, 0x74)),
        ("Filter Type", ONE, Interval(0x00, 0x05)),
        ("Filter Cutoff Scaling Depth", ONE, anySeven),
        ("Filter Cutoff Modulation Depth", ONE, Interval(0x00, 0x63)),
        ("Filter Input Gain", ONE, Interval(0x34, 0x4C)),
        ("FEG Attack", ONE, anySeven),
        ("FEG Decay", ONE, anySeven),
        ("FEG Sustain", ONE, anySeven),
        ("FEG Release", ONE, anySeven),
        ("FEG Depth", ONE, anySeven),
        ("FEG Depth Velocity Sense", ONE, anySeven),
        ("RESERVED 2", ONE, anySeven),
        ("Noise OSC Type", ONE, Interval(0x00, 0x0F)),
        ("Mixer Voice Level", ONE, anySeven),
        ("Mixer Noise Level", ONE, anySeven),
        ("Modulator 1 Harmonic", ONE, anySeven),
        ("Modulator 2 Harmonic", ONE, anySeven),
        ("Modulator 3 Harmonic", ONE, anySeven),
        ("Modulator 1 FM Depth", ONE, anySeven),
        ("Modulator 2 FM Depth", ONE, anySeven),
        ("Modulator 3 FM Depth", ONE, anySeven),
        ("Modulator 1 EG Decay", ONE, anySeven),
        ("Modulator 2 EG Decay", ONE, anySeven),
        ("Modulator 3 EG Decay", ONE, anySeven),
        ("AEG Attack", ONE, anySeven),
        ("AEG Decay", ONE, anySeven),
        ("AEG Sustain", ONE, anySeven),
        ("AEG Release", ONE, anySeven)
      )
    ),
    DataTable(VOICE_COMMON_2, 0x05,
      Seq(
        ("Modulator Select", ONE, Interval(0x00, 0x03)),
        ("Scene Control", ONE, anySeven),
        ("Common Tempo", MSB, Interval(0x00, 0x4A)),
        ("Common Tempo", LSB, anySeven),
        ("Play Effect Swing", ONE, Interval(0x32, 0x53))
      )
    ),
    DataTable(VOICE_SCENE, 0x1C,
      Seq(
        ("Filter Cutoff", ONE, anySeven),
        ("Filter Resonance(Q)", ONE, Interval(0x00, 0x74)),
        ("FEG Attack", ONE, anySeven),
        ("FEG Decay", ONE, anySeven),
        ("FEG Sustain", ONE, anySeven),
        ("FEG Release", ONE, anySeven),
        ("FEG Depth", ONE, anySeven),
        ("Filter Type", ONE, Interval(0x00, 0x05)),
        ("LFO Speed", ONE, Interval(0x00, 0x63)),
        ("Portamento Time", ONE, Interval(0x00, 0x63)),
        ("Mixer Noise Level", ONE, anySeven),
        ("Modulator 1 Harmonic", ONE, anySeven),
        ("Modulator 2 Harmonic", ONE, anySeven),
        ("Modulator 3 Harmonic", ONE, anySeven),
        ("Modulator 1 FM Depth", ONE, anySeven),
        ("Modulator 2 FM Depth", ONE, anySeven),
        ("Modulator 3 FM Depth", ONE, anySeven),
        ("Modulator 1 EG Decay", ONE, anySeven),
        ("Modulator 2 EG Decay", ONE, anySeven),
        ("Modulator 3 EG Decay", ONE, anySeven),
        ("AEG Attack", ONE, anySeven),
        ("AEG Decay", ONE, anySeven),
        ("AEG Sustain", ONE, anySeven),
        ("AEG Release", ONE, anySeven),
        ("Volume", ONE, anySeven),
        ("Pan", ONE, anySeven),
        ("Effect Send", ONE, anySeven),
        ("Effect Parameter", ONE, anySeven)
      )
    ),
    DataTable(VOICE_FREE_EG, 0x60C,
      Seq(
        ("Free EG Trigger", ONE, Interval(0x00, 0x03)),
        ("Free EG Loop Type", ONE, Interval(0x00, 0x04)),
        ("Free EG Length", ONE, Interval(0x02, 0x60)),
        ("Free EG Keyboard Track", ONE, anySeven)
      ) ++ trackParams ++ trackDatas
    ),
    DataTable(VOICE_STEP_SEQ, 0x66,
      Seq(
        ("Step Seq Base Unit", ONE, Discrete(Set(0x04, 0x06, 0x07))),
        ("Step Seq Length", ONE, Discrete(Set(0x08, 0x0C, 0x10))),
        ("RESERVED 1", ONE, anySeven),
        ("RESERVED 2", ONE, anySeven),
        ("RESERVED 3", ONE, anySeven),
        ("RESERVED 4", ONE, anySeven)
      ) ++
        sixteen("Step Seq Note", ONE, anySeven) ++
        sixteen("Step Seq Velocity", ONE, anySeven) ++
        sixteen("Step Seq Gate Time", LSB, anySeven) ++
        sixteen("Step Seq Control Change", ONE, anySeven) ++
        sixteen("Step Seq Gate Time", MSB, anySeven) ++
        sixteen("Step Seq Mute", ONE, onOff)
    ),
    DataTable(EFFECT, 0x03,
      Seq(
        // See effect lookup table
        ("Effect Type", MSB, Discrete(Set(0x00, 0x01, 0x02, 0x03))),
        ("Effect Type", LSB, Discrete(Set(0x00, 0x01, 0x02, 0x03))),
        ("Effect Parameter", ONE, anySeven)
      )
    ),
    DataTable(PART_MIX, 0x0F,
      Seq(
        reserved(1),
        reserved(2),
        reserved(3),
        reserved(4),
        reserved(5),
        ("Volume", ONE, anySeven),
        ("Pan", ONE, anySeven),
        ("Effect 1 Send", ONE, anySeven),
        reserved(6),
        reserved(7),
        ("Filter Cutoff Frequency", ONE, anySeven),
        ("Filter Resonance", ONE, anySeven),
        reserved(8),
        reserved(9),
        reserved(10)
      )
    ),
    DataTable(RHYTHM_STEP_SEQ, 0x66,
      Seq(
        reserved(1),
        reserved(2),
        reserved(3),
        reserved(4),
        reserved(5),
        reserved(6)
      ) ++
        sixteen("Step Seq Instrument", ONE, Interval(0x00, 0x78)) ++
        sixteen("Step Seq Velocity", ONE, anySeven) ++
        sixteen("Step Seq Gate Time", LSB, anySeven) ++
        sixteen("Step Seq Pitch", ONE, anySeven) ++
        sixteen("Step Seq Gate Time", MSB, Interval(0x00, 0x07)) ++
        sixteen("Step Seq Mute", ONE, onOff)
    ),
    DataTable(SONG_DATA, 0x0B,
      Seq(
        ("Pattern Num", MSB, anySeven),
        ("Pattern Num", LSB, anySeven),
        ("BPM", MSB, anySeven),
        ("BPM", LSB, anySeven),
        ("Play FX Gate Time", MSB, anySeven),
        ("Play FX Gate Time", LSB, anySeven),
        ("Beat", ONE, Discrete(Set(0x00, 0x01, 0x02, 0x03, 0x7F))),
        ("Swing", ONE, Union(Interval(0x32, 0x53), Discrete(Set(0x7F)))),
        ("Pitch", ONE, Union(Interval(0x28, 0x58), Discrete(Set(0x7F)))),
        ("Loop Type", ONE, Discrete(Set(0x00, 0x01, 0x7F))),
        ("Track Mute", ONE, Union(Interval(0x00, 0x0F), Discrete(Set(0x7F))))
      )
    )
  )

  private[this] def trackParams: Seq[DataRow] =
    (for {
      i <- 1 until (4 + 1)
    } yield {
      Seq(
        ("Free EG Track Param "+i, ONE, Interval(0x00, 0x1F)),
        ("Free EG Track Scene Switch "+i, ONE, onOff)
      )
    }).flatten

  private[this] def trackDatas: Seq[DataRow] =
    (for {
      i <- 1 until (4 + 1)
    } yield {
      (for {
        j <- 1 until (192 + 1)
      } yield {
        Seq(
          ("Free EG Track "+i+" Data "+j, MSB, topBit),
          ("Free EG Track "+i+" Data "+j, LSB, anySeven)
        )
      }).flatten
    }).flatten

  private[this] def sixteen(name: String, rel: RelType, range: ByteRange): Seq[DataRow] =
    for {
      i <- 1 until (16 + 1)
    } yield {
      (name+" "+i, ONE, range)
    }

  private[this] def getAnnoTable(a: Address): Option[AnnoTable] = {
    var as = Map.newBuilder[AnnoType.Value, Int]
    if (a.low != 0) {
      return None
    } else if (a.modelId == 0x62) {
      as += (AnnoType.PATTERN -> a.mid)
      if (a.high == 0x20) {
        return Some(AnnoTable(as.result, tableMap(VOICE_COMMON_1)))
      } else if (a.high == 0x21) {
        return Some(AnnoTable(as.result, tableMap(VOICE_COMMON_2)))
      } else if (a.high == 0x40 || a.high == 0x41) {
        return Some(AnnoTable(as.result, tableMap(VOICE_SCENE)))
      } else if (a.high >= 0x30 && a.high < 0x40) {
        return Some(AnnoTable(as.result, tableMap(VOICE_FREE_EG)))
      } else if (a.high == 0x50) {
        return Some(AnnoTable(as.result, tableMap(VOICE_STEP_SEQ)))
      }
    } else if (a.modelId == 0x6D) {
      if (a.high >= 0x20 && a.high < 0x30) {
        as += (AnnoType.PART -> (a.high & 0x0F))
        as += (AnnoType.PATTERN-> a.mid)
        return Some(AnnoTable(as.result, tableMap(RHYTHM_STEP_SEQ)))
      } else if (a.high == 0x30) {
        as += (AnnoType.PATTERN-> a.mid)
        return Some(AnnoTable(as.result, tableMap(EFFECT)))
      } else if (a.high >= 0x40 && a.high < 0x50) {
        as += (AnnoType.PART -> (a.high & 0x0F))
        as += (AnnoType.PATTERN-> a.mid)
        return Some(AnnoTable(as.result, tableMap(PART_MIX)))
      } else if (a.high >= 0x60 && a.high < 0x70) {
        as += (AnnoType.SONG-> (a.high & 0x0F))
        as += (AnnoType.MEASURE -> a.mid)
        return Some(AnnoTable(as.result, tableMap(SONG_DATA)))
      } else if (a.high >= 0x70 && a.high < 0x80) {
        as += (AnnoType.SONG-> (a.high & 0x0F))
        as += (AnnoType.MEASURE -> (a.mid + 0x7F))
        return Some(AnnoTable(as.result, tableMap(SONG_DATA)))
      }
    }
    None
  }

  private[this] lazy val onOff: ByteRange = Discrete(Set(0x00, 0x01))
  private[this] lazy val topBit: ByteRange = onOff
  private[this] lazy val anySeven: ByteRange = Interval(0x00, 0x7F)

  private[this] def reserved(i: Int): DataRow =
    ("RESERVED "+i, ONE, anySeven)

  lazy val tableMap: Map[DataType, DataTable] =
    (for { t <- tables } yield { (t.name, t) }).toMap
}
