package net.rouly.employability.analysis.lda

import net.rouly.common.config.Configuration

class LdaConfig(configuration: Configuration) {

  private val sub = configuration.sub("lda")

  lazy val jdbcUrl: String = "jdbc:postgresql://localhost:5432/employability"
  lazy val jdbcTable: String = "documents"
  lazy val jdbcUser: String = "employability"
  lazy val jdbcPassword: String = "employability"

  lazy val numberTopics: Int = sub.getInt("k", 10)
  lazy val maxIterations: Int = sub.getInt("max.iterations", 10)
  lazy val wordsPerTopic: Int = sub.getInt("words.per.topic", 10)

  /**
    * Specifies the minimum number of different documents a term must appear in to be included
    * in the vocabulary.
    * If this is an integer greater than or equal to 1, this specifies the number of documents
    * the term must appear in; if this is a double in [0,1), then this specifies the fraction of
    * documents.
    *
    * @see [[org.apache.spark.ml.feature.CountVectorizer.minDF]]
    */
  lazy val minDocumentFrequency: Double = sub.get("min.document.frequency", "1.0").toDouble

  /**
    * Filter to ignore rare words in a document. For each document, terms with
    * frequency/count less than the given threshold are ignored.
    * If this is an integer greater than or equal to 1, then this specifies a count (of times the
    * term must appear in the document);
    * if this is a double in [0,1), then this specifies a fraction (out of the document's token
    * count).
    *
    * @see [[org.apache.spark.ml.feature.CountVectorizer.minTF]]
    */
  lazy val minTermFrequency: Double = sub.get("min.term.frequency", "1.0").toDouble

  /**
    * Max size of the vocabulary.
    * CountVectorizer will build a vocabulary that only considers the top
    * vocabSize terms ordered by term frequency across the corpus.
    *
    * @see [[org.apache.spark.ml.feature.CountVectorizer.vocabSize]]
    */
  lazy val vocabularySize: Int = sub.getInt("vocab.size", 2e18.toInt)

}
