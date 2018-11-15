package net.rouly.employability.preprocess.transform.preprocess

import scala.io.Source

class StopWordRemover {

  def isStopWord(token: String): Boolean = StopWordRemover.StopWords.contains(token)

}

object StopWordRemover {
  private final val StopWords: Set[String] = loadStopWords()

  private def loadStopWords(): Set[String] = {
    val stream = getClass.getResourceAsStream("/en-stop-words.txt")
    Source.fromInputStream(stream).getLines().toSet
  }
}
