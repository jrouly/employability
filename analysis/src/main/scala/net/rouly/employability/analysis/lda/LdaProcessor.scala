package net.rouly.employability.analysis.lda

import akka.actor.ActorSystem
import org.apache.spark.ml.clustering.LDA
import org.apache.spark.sql.{DataFrame, SparkSession}

class LdaProcessor(
  spark: SparkSession,
  config: LdaConfig
)(implicit actorSystem: ActorSystem) {

  protected def streamingDataFrame: DataFrame = {
    spark
      .readStream
      .format("org.apache.bahir.sql.streaming.akka.AkkaStreamSourceProvider")
      .option("urlOfPublisher", config.publisherUrl)
      .load()
  }

  def execute: DataFrame = {
    val dataset = streamingDataFrame
    lazy val model = new LDA()
      .setK(config.numberTopics)
      .setMaxIter(config.maxIterations)
      .fit(dataset)

    // TODO: What is this?
    // val transformed: DataFrame = model.transform(dataset)

    // Return topics.
    // model.describeTopics(config.wordsPerTopic)

    println(dataset)
    dataset
  }

}
