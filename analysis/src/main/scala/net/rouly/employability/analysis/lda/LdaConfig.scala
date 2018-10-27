package net.rouly.employability.analysis.lda

import net.rouly.common.config.Configuration

class LdaConfig(configuration: Configuration) {

  private val sub = configuration.sub("lda")

  val jdbcUrl: String = "jdbc:postgresql://localhost:5432/employability"
  val jdbcTable: String = "documents"
  val jdbcUser: String = "employability"
  val jdbcPassword: String = "employability"

  val numberTopics: Int = sub.getInt("k", 10)
  val maxIterations: Int = sub.getInt("max.iterations", 10)
  val wordsPerTopic: Int = sub.getInt("words.per.topic", 10)
  val timeoutMillis: Long = sub.getInt("timeout", 10)

}
