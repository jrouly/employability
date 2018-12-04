package net.rouly.employability.models.lda

import net.rouly.employability.models.WeightedTopic

case class LdaOutputDocument(
  id: String,
  weightedTopics: List[WeightedTopic]
)
