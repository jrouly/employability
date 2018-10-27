package net.rouly.employability.analysis.lda

import akka.actor.ActorSystem
import org.apache.spark.ml.clustering.LDA
import org.apache.spark.sql.{DataFrame, SparkSession}

class LdaProcessor(
  spark: SparkSession,
  config: LdaConfig
)(implicit actorSystem: ActorSystem) {

  protected def readData: DataFrame = {
    spark.read
      .format("jdbc")
      .option("url", config.jdbcUrl)
      .option("dbtable", config.jdbcTable)
      .option("user", config.jdbcUser)
      .option("password", config.jdbcPassword)
      .load()
      .withColumnRenamed("id", "label")
      .withColumnRenamed("content", "features")
  }

  def execute(): Unit = {
    lazy val model = new LDA()
      .setK(config.numberTopics)
      .setMaxIter(config.maxIterations)
      .fit(readData)

    // TODO: What is this?
    // val transformed: DataFrame = model.transform(dataset)

    // Return topics.
    val topics = model.describeTopics(config.wordsPerTopic)

    topics.show()
  }

}
