package net.rouly.employability.preprocess.transform.preprocess

import scala.io.Source

object StopWordRemover {

  def isStopWord(token: String): Boolean = StopWords.contains(token)

  private final val StopWords: Set[String] = loadStopWords()

  private def loadStopWords(): Set[String] = {
    val stream = getClass.getResourceAsStream("/en-stop-words.txt")
    Source.fromInputStream(stream).getLines().toSet
  }
}
