package net.rouly.employability.analysis.lda

import akka.actor.ActorSystem
import net.rouly.employability.models.{ModeledDocument, Topic}
import org.apache.spark.ml.clustering.{LDA, LDAModel}
import org.apache.spark.ml.feature._
import org.apache.spark.ml.linalg
import org.apache.spark.sql._

import scala.util.control.NonFatal

class LdaProcessor(
  spark: SparkSession,
  config: LdaConfig
)(implicit actorSystem: ActorSystem) {
  import spark.implicits._

  /**
    * Read in data from the JDBC where it's cached after streaming in from Elasticsearch.
    */
  protected def readData: DataFrame = {
    spark.read
      .format("jdbc")
      .option("url", config.jdbcUrl)
      .option("dbtable", config.jdbcTable)
      .option("user", config.jdbcUser)
      .option("password", config.jdbcPassword)
      .option("driver", "org.postgresql.Driver")
      .load()
  }

  /**
    * Tokenize and vectorize the term data.
    * @return (vectorized term data, vocabulary)
    */
  protected def vectorize(df: DataFrame): (DataFrame, Array[String]) = {
    val tokens = new RegexTokenizer()
      .setPattern("\\|")
      .setInputCol("content")
      .setOutputCol("tokens")
      .transform(df)

    val filtered = new StopWordsRemover()
      .setCaseSensitive(false)
      .setInputCol("tokens")
      .setOutputCol("filtered")
      .transform(tokens)

    val (countVectors, vocabulary) = {
      val model = new CountVectorizer()
        .setMinDF(config.minDocumentFrequency)
        .setMinTF(config.minTermFrequency)
        .setVocabSize(config.vocabularySize)
        .setInputCol("filtered")
        .setOutputCol("rawFeatures")
        .fit(filtered)

      (model.transform(filtered), model.vocabulary)
    }

    val idf = new IDF()
      .setInputCol("rawFeatures")
      .setOutputCol("features")
      .fit(countVectors)
      .transform(countVectors)

    (idf, vocabulary)
  }

  /**
    * Extract a collection of consumable topics from the [[LDAModel]] and vocabulary.
    */
  private def getTopics(model: LDAModel, vocabulary: Array[String]) = {
    val topics = model.describeTopics(config.wordsPerTopic)
    topics.map {
      case Row(id: Int, termIndices, termWeights) =>
        val termIndicesSeq = termIndices.asInstanceOf[Seq[Int]]
        val termWeightsSeq = termWeights.asInstanceOf[Seq[Double]]
        val wordFrequency = termIndicesSeq
          .map(vocabulary.apply)
          .zip(termWeightsSeq)
          .toMap
        Topic(id.toString, wordFrequency)
      case _ =>
        throw new Exception("Unable to parse topics.")
    }
  }

  /**
    * Associate modeled documents with their topic distributions.
    */
  private def getModeledDocuments(df: DataFrame) = {
    df.map {
      case Row(id: String, raw: String, _, _, filtered, _, _, distribution: linalg.Vector) =>
        ModeledDocument(
          id = id,
          originalText = raw,
          tokens = filtered.asInstanceOf[Seq[String]],
          topicWeight = distribution
            .toArray
            .zipWithIndex
            .map { case (weight, topic) => topic.toString -> weight }
            .toMap
        )
      case _ =>
        throw new Exception("Unable to parse modeled topics.")
    }
  }

  /**
    * Perform LDA topic modeling on the input corpus.
    */
  def execute(): Unit = {
    try {

      // Perform vectorization.
      val (df, vocabulary) = vectorize(readData)

      // Set up LDA.
      val lda = new LDA()
        .setK(config.numberTopics)
        .setMaxIter(config.maxIterations)

      // Execute LDA.
      val model = lda.fit(df)
      val modeled = model.transform(df)

      // Extract results.
      val topics = getTopics(model, vocabulary)
      val modeledDocuments = getModeledDocuments(modeled)

      // Emit results.
      topics.foreach(topic => LdaQueues.emit(topic))
      modeledDocuments.foreach(document => LdaQueues.emit(document))

      // Return.
      ()

    } catch {
      case NonFatal(e) => // catch-all
        e.printStackTrace()
    }
  }

}
