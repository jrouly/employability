package net.rouly.employability.analysis.lda

import akka.actor.ActorSystem
import com.typesafe.scalalogging.Logger
import net.rouly.employability.models._
import org.apache.spark.ml.clustering.{LDA, LDAModel}
import org.apache.spark.ml.feature._
import org.apache.spark.ml.linalg
import org.apache.spark.sql._
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._
import scala.util.control.NonFatal

class LdaProcessor(
  spark: SparkSession,
  config: LdaConfig
)(implicit actorSystem: ActorSystem) {
  import spark.implicits._

  /**
    * Read in data from the JDBC where it's cached after streaming in from Elasticsearch.
    */
  protected def readDataFromJdbc: DataFrame = {
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
    topics.flatMap {
      case Row(id: Int, termIndices, termWeights) =>
        val termIndicesSeq = termIndices.asInstanceOf[Seq[Int]]
        val termWeightsSeq = termWeights.asInstanceOf[Seq[Double]]
        val wordFrequency = termIndicesSeq
          .map(vocabulary.apply)
          .zip(termWeightsSeq)
          .map((WordFrequency.apply _).tupled)
          .toSet
        Some(Topic(id.toString, wordFrequency))
      case error =>
        val logger = Logger(LoggerFactory.getLogger("LdaProcessor"))
        logger.error(s"Unable to parse topic. [$error]")
        None
    }
  }

  /**
    * Associate modeled documents with their topic distributions.
    */
  private def getModeledDocuments(df: DataFrame, topics: Vector[Topic]) = {
    df.flatMap {
      case Row(id: String, raw: String, _, kind: String, dataSet: String, _, filtered, _, _, distribution: linalg.Vector) =>
        Some(ModeledDocument(
          id = id,
          kind = kind,
          dataSet = dataSet,
          originalText = raw,
          tokens = filtered.asInstanceOf[Seq[String]],
          weightedTopics = distribution
            .toArray
            .zipWithIndex
            .map { case (weight, topic) => WeightedTopic(topics(topic), weight) }
            .toList
        ))
      case error =>
        val logger = Logger(LoggerFactory.getLogger("LdaProcessor"))
        logger.error(s"Unable to parse modeled document. [$error]")
        None
    }
  }

  /**
    * Perform LDA topic modeling on the input corpus.
    */
  def execute(): Unit = {
    try {

      // Perform vectorization.
      val (df, vocabulary) = vectorize(readDataFromJdbc)

      // Set up LDA.
      val lda = new LDA()
        .setK(config.numberTopics)
        .setMaxIter(config.maxIterations)

      // Execute LDA.
      val model = lda.fit(df)
      val modeled = model.transform(df)

      // Extract results.
      val topics = getTopics(model, vocabulary)
      val topicVector = topics.toLocalIterator().asScala.toVector
      val modeledDocuments = getModeledDocuments(modeled, topicVector)

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
