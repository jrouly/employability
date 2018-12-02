package net.rouly.employability.models

import play.api.libs.json.{Format, Json}

/**
  * @param id unique id of the topic
  * @param wordFrequency map of word to frequency
  */
case class Topic(
  id: String,
  wordFrequency: Set[WordFrequency]
)

object Topic {
  implicit val format: Format[Topic] = Json.format[Topic]
}

case class WordFrequency(
  word: String,
  frequency: Double
)

object WordFrequency {
  implicit val format: Format[WordFrequency] = Json.format[WordFrequency]
}
