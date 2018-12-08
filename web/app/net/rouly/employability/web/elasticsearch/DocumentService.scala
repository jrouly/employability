package net.rouly.employability.web.elasticsearch

import akka.NotUsed
import akka.stream.scaladsl.Source
import com.sksamuel.elastic4s.http.ElasticDsl._
import com.sksamuel.elastic4s.http.Response
import com.sksamuel.elastic4s.http.count.CountResponse
import com.sksamuel.elastic4s.playjson._
import net.rouly.employability.elasticsearch.ElasticsearchModule
import net.rouly.employability.models.ModeledDocument

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

class DocumentService(elasticsearch: ElasticsearchModule)(implicit ec: ExecutionContext) {

  import elasticsearch.client.execute

  def documentCount: Future[Response[CountResponse]] = execute {
    count(elasticsearch.config.modeledDocumentIndex)
  }

  def documentSource: Source[ModeledDocument, NotUsed] = {
    elasticsearch.streams
      .source(elasticsearch.config.modeledDocumentIndex)
      .map(_.to[ModeledDocument])
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

}
