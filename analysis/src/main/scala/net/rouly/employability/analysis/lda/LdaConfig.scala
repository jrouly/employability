package net.rouly.employability.analysis.lda

import net.rouly.common.config.Configuration

class LdaConfig(configuration: Configuration) {

  private val sub = configuration.sub("lda")

  val numberTopics: Int = sub.getInt("k", 10)
  val maxIterations: Int = sub.getInt("max.iterations", 10)
  val wordsPerTopic: Int = sub.getInt("words.per.topic", 10)
  val timeoutMillis: Long = sub.getInt("timeout", 10)

}
