package net.rouly.employability.web.application.model

import play.api.libs.json.{Json, Reads}

case class OverlapStats(entries: List[OverlapEntry])
case class OverlapEntry(kind: String, topicCounts: List[OverlapTopicCount])
case class OverlapTopicCount(topicId: String, documentCount: Int)

object OverlapStats {

  case class Buckets(key: String, doc_count: Double)
  case class RelevantTopicIdsAggregation(buckets: List[Buckets])
  case class RelevantTopics(relevant_topic_ids: RelevantTopicIdsAggregation)
  case class TopicsAggregation(relevantTopics: RelevantTopics)
  case class KindsAggregation(key: String, topics: TopicsAggregation)
  case class KindsAggregationList(buckets: List[KindsAggregation])
  case class OverlapAggregation(kinds: KindsAggregationList) {
    val overlapStats: OverlapStats = OverlapStats(
      entries = kinds.buckets.map(bucket =>
        OverlapEntry(
          kind = bucket.key,
          topicCounts = bucket
            .topics
            .relevantTopics
            .relevant_topic_ids
            .buckets
            .map(relevantTopicId => OverlapTopicCount(relevantTopicId.key, relevantTopicId.doc_count.toInt))
        ))
    )
  }

  implicit val readsBuckets: Reads[Buckets] = Json.reads[Buckets]
  implicit val readsRelevantTopicIdsAggregation: Reads[RelevantTopicIdsAggregation] = Json.reads[RelevantTopicIdsAggregation]
  implicit val readsRelevantTopics: Reads[RelevantTopics] = Json.reads[RelevantTopics]
  implicit val readsTopicsAggregation: Reads[TopicsAggregation] = Json.reads[TopicsAggregation]
  implicit val readsKindsAggregation: Reads[KindsAggregation] = Json.reads[KindsAggregation]
  implicit val readsKindsAggregationList: Reads[KindsAggregationList] = Json.reads[KindsAggregationList]
  implicit val readsOverlapAggregation: Reads[OverlapAggregation] = Json.reads[OverlapAggregation]

}
