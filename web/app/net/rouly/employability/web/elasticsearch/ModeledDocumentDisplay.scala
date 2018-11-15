package net.rouly.employability.web.elasticsearch

import play.api.libs.json.{Format, Json}

case class ModeledDocumentDisplay(
  id: String,
  originalText: String,
  topicWeight: Map[String, Double]
)

object ModeledDocumentDisplay {
  implicit val format: Format[ModeledDocumentDisplay] = Json.format[ModeledDocumentDisplay]
}
