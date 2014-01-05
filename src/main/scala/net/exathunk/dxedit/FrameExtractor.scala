package net.exathunk.dxedit

import scala.util.{Success, Failure, Try}

sealed trait ExtractException extends Exception
case object FrameMismatchException extends ExtractException
case object OverlapException extends ExtractException
case object ContiguityException extends ExtractException

object FrameExtractor {
  def apply(bytes: Seq[Byte]): Try[Seq[Frame]] = {
    val starts = bytes.zipWithIndex.collect { case(Constants.START_TAG, i) => i }
    val ends = bytes.zipWithIndex.collect { case(Constants.END_TAG, i) => i }
    if (starts.size != ends.size) {
      Failure(FrameMismatchException)
    } else {
      val ivals = starts.zip(ends)
      val b = Seq.newBuilder[Frame]
      var exc: Option[ExtractException] = None
      var lastIndex = -1
      ivals.foreach { ival =>
        if (ival._1 > ival._2) {
          exc = Some(OverlapException)
        } else if (ival._1 != lastIndex + 1) {
          exc = Some(ContiguityException)
        } else {
          b += bytes.slice(ival._1, ival._2 + 1)
          lastIndex = ival._2
        }
      }
      if (exc.isDefined) {
        Failure(exc.get)
      } else if (lastIndex != bytes.size - 1) {
        Failure(ContiguityException)
      } else {
        Success(b.result)
      }
    }
  }
}
