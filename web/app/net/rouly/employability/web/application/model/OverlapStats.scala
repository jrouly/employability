package net.rouly.employability.web.application.model

import net.rouly.employability.models.DocumentKind.CourseDescription
import play.api.libs.json.{Json, Reads}

case class OverlapStats(entries: List[OverlapEntry])
case class OverlapEntry(
  topicId: Int,
  jobDescriptionCount: Int,
  jobDescriptionProportion: Double,
  courseDescriptionCount: Int,
  courseDescriptionProportion: Double
)

object OverlapStats {

  case class Buckets(key: String, doc_count: Double)
  case class RelevantTopicIdsAggregation(buckets: List[Buckets])
  case class RelevantTopics(relevant_topic_ids: RelevantTopicIdsAggregation)
  case class TopicsAggregation(relevantTopics: RelevantTopics)
  case class KindsAggregation(key: String, topics: TopicsAggregation)
  case class KindsAggregationList(buckets: List[KindsAggregation])
  case class OverlapAggregation(kinds: KindsAggregationList) {

    val overlapStats: OverlapStats = {
      type TopicAggregation = List[KindsAggregation]
      val (cds, jds) = kinds.buckets.partition(_.key == CourseDescription.kind)
      def docCount(aggregation: TopicAggregation): Int = aggregation
        .flatMap(_.topics.relevantTopics.relevant_topic_ids.buckets)
        .map(_.doc_count)
        .sum.toInt
      def docProportion(topicCount: Int, aggregation: TopicAggregation): Double =
        topicCount.toDouble / docCount(aggregation).toDouble
      val topicIds = kinds.buckets
        .flatMap(_.topics.relevantTopics.relevant_topic_ids.buckets)
        .map(_.key)
        .distinct
      val entries = topicIds.map { topicId =>
        def topicCount(aggregation: TopicAggregation): Int = aggregation
          .flatMap(_.topics.relevantTopics.relevant_topic_ids.buckets)
          .find(_.key == topicId)
          .fold(0)(_.doc_count.toInt)
        val jdTopicCount = topicCount(jds)
        val cdTopicCount = topicCount(cds)
        OverlapEntry(
          topicId = topicId.toInt,
          jobDescriptionCount = topicCount(jds),
          jobDescriptionProportion = docProportion(jdTopicCount, jds),
          courseDescriptionCount = topicCount(cds),
          courseDescriptionProportion = docProportion(cdTopicCount, cds)
        )
      }
      OverlapStats(entries)
    }

  }

  implicit val readsBuckets: Reads[Buckets] = Json.reads[Buckets]
  implicit val readsRelevantTopicIdsAggregation: Reads[RelevantTopicIdsAggregation] = Json.reads[RelevantTopicIdsAggregation]
  implicit val readsRelevantTopics: Reads[RelevantTopics] = Json.reads[RelevantTopics]
  implicit val readsTopicsAggregation: Reads[TopicsAggregation] = Json.reads[TopicsAggregation]
  implicit val readsKindsAggregation: Reads[KindsAggregation] = Json.reads[KindsAggregation]
  implicit val readsKindsAggregationList: Reads[KindsAggregationList] = Json.reads[KindsAggregationList]
  implicit val readsOverlapAggregation: Reads[OverlapAggregation] = Json.reads[OverlapAggregation]

}
