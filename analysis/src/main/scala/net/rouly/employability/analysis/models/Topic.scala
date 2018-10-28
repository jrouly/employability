package net.rouly.employability.analysis.models

case class Topic(
  id: Int,
  wordFrequency: Map[String, Double]
)
