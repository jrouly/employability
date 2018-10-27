package net.rouly.employability.analysis.models

import java.util.UUID

/**
  * A document to be modeled.
  *
  * @param id unique identifier
  * @param content content of the document
  */
case class Document[T](
  id: UUID,
  content: T
)
