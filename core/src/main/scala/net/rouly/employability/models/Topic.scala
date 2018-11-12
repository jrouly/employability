package net.rouly.employability.models

/**
  * @param id unique id of the topic
  * @param wordFrequency map of word to frequency
  */
case class Topic(
  id: String,
  wordFrequency: Map[String, Double]
)
