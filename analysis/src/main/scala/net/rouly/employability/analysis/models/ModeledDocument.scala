package net.rouly.employability.analysis.models

case class ModeledDocument(
  id: String,
  originalText: String,
  tokens: Seq[String],
  topicWeight: Map[Int, Double]
)
