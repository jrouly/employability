package net.rouly.employability.models

import play.api.libs.json.{Format, Json}

/**
  * @param id corresponds to original document ID
  * @param originalText orignial text of the document
  * @param tokens raw tokens
  * @param topicWeight map of topic ID to proportional weight
  */
case class ModeledDocument(
  id: String,
  originalText: String,
  tokens: Seq[String],
  topicWeight: Map[String, Double]
)

object ModeledDocument {
  implicit val modeledDocumentFormat: Format[ModeledDocument] = Json.format[ModeledDocument]
}
