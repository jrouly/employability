package net.rouly.employability.web.elasticsearch

import akka.NotUsed
import akka.stream.scaladsl.Source
import com.sksamuel.elastic4s.http.ElasticDsl._
import com.sksamuel.elastic4s.http.Response
import com.sksamuel.elastic4s.http.count.CountResponse
import com.sksamuel.elastic4s.playjson._
import com.sksamuel.elastic4s.searches.SearchRequest
import net.rouly.employability.elasticsearch.ElasticsearchModule
import net.rouly.employability.models.{ModeledDocument, Topic}
import net.rouly.employability.web.application.model.BucketEntry

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

class ElasticsearchWebService(elasticsearch: ElasticsearchModule)(implicit ec: ExecutionContext) {
  import elasticsearch.client.execute

  /**
    * Read all topics from Elasticsearch and return a future with them.
    */
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
      .map(_
        .result
        .aggregations
        .terms(aggName)
        .buckets
        .map(bucket => BucketEntry(bucket.key, bucket.docCount)))
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

}
