package net.rouly.employability.web.elasticsearch

import akka.NotUsed
import akka.stream.scaladsl.Source
import com.sksamuel.elastic4s.http.ElasticDsl._
import com.sksamuel.elastic4s.http.Response
import com.sksamuel.elastic4s.http.count.CountResponse
import com.sksamuel.elastic4s.playjson._
import net.rouly.employability.elasticsearch.ElasticsearchModule
import net.rouly.employability.models.{ModeledDocument, Topic}

import scala.concurrent.Future
import scala.concurrent.duration._

class ElasticsearchWebService(elasticsearch: ElasticsearchModule) {

  /**
    * Read all topics from Elasticsearch and return a future with them.
    */
  def topicSource: Source[Topic, NotUsed] = {
    elasticsearch.streams
      .source(elasticsearch.config.topicIndex)
      .map(_.to[Topic])
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

  def documentCount: Future[Response[CountResponse]] = {
    import com.sksamuel.elastic4s.http.ElasticDsl._
    elasticsearch.client.execute {
      count(elasticsearch.config.modeledDocumentIndex)
    }
  }

}
