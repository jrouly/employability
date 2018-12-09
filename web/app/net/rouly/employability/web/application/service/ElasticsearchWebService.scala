package net.rouly.employability.web.application.service

import com.sksamuel.elastic4s.AggReader
import com.sksamuel.elastic4s.http.ElasticDsl._
import com.sksamuel.elastic4s.searches.SearchRequest
import net.rouly.employability.elasticsearch.ElasticsearchModule
import net.rouly.employability.models.DocumentKind
import net.rouly.employability.web.application.model.OverlapStats._
import net.rouly.employability.web.application.model.{BucketEntry, OverlapStats}
import play.api.libs.json._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class ElasticsearchWebService(elasticsearch: ElasticsearchModule)(implicit ec: ExecutionContext) {

  import elasticsearch.client.execute

  protected def bucket(aggName: String, searchRequest: SearchRequest): Future[Seq[BucketEntry]] = {
    execute(searchRequest)
      .map(
        _
          .result
          .aggregations
          .terms(aggName)
          .buckets
          .map(bucket => BucketEntry(bucket.key, bucket.docCount))
          .sortBy(_.dataSet)
      )
  }

  def bucket(field: String, filter: (String, String)): Future[Seq[BucketEntry]] = {
    val aggName = s"$field-agg"
    val searchRequest = search(elasticsearch.config.modeledDocumentIndex)
      .termQuery(filter._1, filter._2)
      .size(0)
      .aggregations(termsAggregation(aggName).field(field).size(50))
    bucket(aggName, searchRequest)
  }

  def bucket(field: String, rawDocuments: Boolean = false): Future[Seq[BucketEntry]] = {
    val aggName = s"$field-agg"
    val index = if (rawDocuments) elasticsearch.config.rawDocumentIndex else elasticsearch.config.modeledDocumentIndex
    val searchRequest = search(index).size(0).aggregations(termsAggregation(aggName).field(field).size(50))
    bucket(aggName, searchRequest)
  }

  private implicit def jsonAggregationsReader[T: Reads]: AggReader[T] = new AggReader[T] {
    override def read(json: String): Try[T] = Try(Json.parse(json).validate[T].get)
  }

  def countByKind(kind: DocumentKind): Future[Long] =
    execute(count(elasticsearch.config.modeledDocumentIndex).query(termQuery("kind", kind.kind)))
      .map(_.result.count)

  def overlap(jdCount: Long, cdCount: Long, rho: Double): Future[OverlapStats] = {
    execute {
      val kinds = termsAggregation("kinds").field("kind").size(2)
      val topics = nestedAggregation("topics", path = "weightedTopics")
      val relevantTopics = filterAggregation("relevantTopics").query(rangeQuery("weightedTopics.weight").gt(rho))
      val relevantTopicIds = termsAggregation("relevant_topic_ids").field("weightedTopics.topic.id").size(1000)
      val aggs = kinds.subaggs(topics.subaggs(relevantTopics.subaggs(relevantTopicIds)))
      search(elasticsearch.config.modeledDocumentIndex).size(0).aggregations(aggs)
    }.map { response => response.result.aggregations.to[OverlapAggregation].overlapStats(jdCount, cdCount) }
  }

}
