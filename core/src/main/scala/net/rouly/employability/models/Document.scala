package net.rouly.employability.models

import java.util.UUID

/**
  * A document to be modeled.
  *
  * @param id unique identifier
  * @param raw original document text
  * @param content content of the document
  */
case class Document[T](
  id: UUID,
  raw: String,
  content: T
)
