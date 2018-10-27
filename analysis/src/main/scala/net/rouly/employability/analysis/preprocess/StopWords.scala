package net.rouly.employability.analysis.preprocess

class StopWords {

  import org.apache.spark.ml.feature.StopWordsRemover

  val defaultStopWords: Array[String] = StopWordsRemover.loadDefaultStopWords("english")

  val remover = new StopWordsRemover()
    .setCaseSensitive(false)
    .set

}
