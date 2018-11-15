package net.rouly.employability.models

import play.api.libs.json.{Format, Json}

/**
  * @param id unique id of the topic
  * @param wordFrequency map of word to frequency
  */
case class Topic(
  id: String,
  wordFrequency: Map[String, Double]
)

object Topic {
  implicit val topicFormat: Format[Topic] = Json.format[Topic]
}
