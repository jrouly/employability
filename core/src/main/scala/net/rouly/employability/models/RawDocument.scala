package net.rouly.employability.models

import java.util.UUID

import play.api.libs.json.{Format, Json}

/**
  * @param id unique identifier
  * @param dataSet which data set the data came from
  * @param description body of the document
  * @param kind kind of document
  * @param title title of the document, if given
  */
case class RawDocument(
  id: UUID,
  dataSet: String,
  description: String,
  kind: String,
  title: Option[String] = None
)

object RawDocument {
  implicit val jobPostingFormat: Format[RawDocument] = Json.format[RawDocument]
}
