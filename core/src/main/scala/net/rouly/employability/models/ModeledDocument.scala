package net.rouly.employability.models

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
