package net.rouly.employability.web.elasticsearch

import akka.NotUsed
import akka.stream.scaladsl.Source
import com.sksamuel.elastic4s.http.ElasticDsl._
import com.sksamuel.elastic4s.playjson._
import net.rouly.employability.elasticsearch.ElasticsearchModule
import net.rouly.employability.models.Topic

import scala.concurrent.duration._

class ElasticsearchService(elasticsearch: ElasticsearchModule) {

  /**
    * Read all topics from Elasticsearch and return a future with them.
    */
  def topicSource: Source[Topic, NotUsed] = {
    elasticsearch.streams
      .source(elasticsearch.config.topicIndex)
      .map(_.to[Topic])
  }

  def documentsByTopic(topicId: String): Source[ModeledDocumentDisplay, NotUsed] = {
    val field = s"topicWeight.$topicId"
    val searchRequest = search(elasticsearch.config.modeledDocumentIndex)
      .query(rangeQuery(field).gt(0.01))
      .sortByFieldDesc(field)
      .scroll(5.seconds)

    elasticsearch.streams
      .source(searchRequest)
      .map(_.to[ModeledDocumentDisplay])
  }

}
