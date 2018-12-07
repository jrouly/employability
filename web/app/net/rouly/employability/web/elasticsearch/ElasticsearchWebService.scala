package net.rouly.employability.web.elasticsearch

import akka.NotUsed
import akka.stream.scaladsl.Source
import com.sksamuel.elastic4s.AggReader
import com.sksamuel.elastic4s.http.ElasticDsl._
import com.sksamuel.elastic4s.http.Response
import com.sksamuel.elastic4s.http.count.CountResponse
import com.sksamuel.elastic4s.playjson._
import com.sksamuel.elastic4s.searches.SearchRequest
import net.rouly.employability.elasticsearch.ElasticsearchModule
import net.rouly.employability.models.{ModeledDocument, Topic}
import net.rouly.employability.web.application.model.{BucketEntry, OverlapStats}
import net.rouly.employability.web.application.model.OverlapStats._
import play.api.libs.json._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class ElasticsearchWebService(elasticsearch: ElasticsearchModule)(implicit ec: ExecutionContext) {

  import elasticsearch.client.execute

  def topicSource: Source[Topic, NotUsed] = {
    elasticsearch.streams
      .source(elasticsearch.config.topicIndex)
      .map(_.to[Topic])
  }

  def topicCount: Future[Response[CountResponse]] = execute {
    count(elasticsearch.config.topicIndex)
  }

  def documentsByTopic(topicId: String): Source[ModeledDocument, NotUsed] = {
    val weightQuery = rangeQuery("weightedTopics.weight").gt(0.5)
    val topicIdQuery = termQuery("weightedTopics.topic.id", topicId)
    val query = nestedQuery("weightedTopics", must(topicIdQuery, weightQuery))

    val searchRequest = search(elasticsearch.config.modeledDocumentIndex)
      .query(query)
      .sortByFieldDesc("weightedTopics.weight")
      .scroll(5.seconds)

    elasticsearch.streams
      .source(searchRequest)
      .map(_.to[ModeledDocument])
  }

  def documentSource: Source[ModeledDocument, NotUsed] = {
    elasticsearch.streams
      .source(elasticsearch.config.modeledDocumentIndex)
      .map(_.to[ModeledDocument])
  }

  def documentCount: Future[Response[CountResponse]] = execute {
    count(elasticsearch.config.modeledDocumentIndex)
  }

  protected def bucket(aggName: String, searchRequest: SearchRequest): Future[Seq[BucketEntry]] = {
    execute(searchRequest)
      .map(
        _
          .result
          .aggregations
          .terms(aggName)
          .buckets
          .map(bucket => BucketEntry(bucket.key, bucket.docCount))
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

  def overlap: Future[OverlapStats] = {
    execute {
      val kinds = termsAggregation("kinds").field("kind").size(2)
      val topics = nestedAggregation("topics", path = "weightedTopics")
      val relevantTopics = filterAggregation("relevantTopics").query(rangeQuery("weightedTopics.weight").gt(0.5))
      val relevantTopicIds = termsAggregation("relevant_topic_ids").field("weightedTopics.topic.id").size(100)
      val aggs = kinds.subaggs(topics.subaggs(relevantTopics.subaggs(relevantTopicIds)))
      search(elasticsearch.config.modeledDocumentIndex).size(0).aggregations(aggs)
    }.map { response => response.result.aggregations.to[OverlapAggregation].overlapStats }
  }

}
