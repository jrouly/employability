package net.rouly.employability.models

import play.api.libs.json.{Format, Json}

/**
  * @param id corresponds to original document ID
  * @param originalText orignial text of the document
  * @param tokens raw tokens
  * @param weightedTopics weighted mixture of topics
  */
case class ModeledDocument(
  id: String,
  originalText: String,
  tokens: Seq[String],
  weightedTopics: List[WeightedTopic]
)

object ModeledDocument {
  implicit val modeledDocumentFormat: Format[ModeledDocument] = Json.format[ModeledDocument]
}

case class WeightedTopic(
  topic: Topic,
  weight: Double
)

object WeightedTopic {
  implicit val format: Format[WeightedTopic] = Json.format[WeightedTopic]
}
