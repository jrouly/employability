package net.rouly.employability.web.elasticsearch

import akka.NotUsed
import akka.stream.scaladsl.Source
import com.sksamuel.elastic4s.http.ElasticDsl._
import com.sksamuel.elastic4s.http.Response
import com.sksamuel.elastic4s.http.count.CountResponse
import com.sksamuel.elastic4s.playjson._
import net.rouly.employability.elasticsearch.ElasticsearchModule
import net.rouly.employability.models.Topic

import scala.concurrent.{ExecutionContext, Future}

class TopicService(elasticsearch: ElasticsearchModule)(implicit ec: ExecutionContext) {

  import elasticsearch.client.execute

  def topicCount: Future[Response[CountResponse]] = execute {
    count(elasticsearch.config.topicIndex)
  }

  def topicSource: Source[Topic, NotUsed] = {
    elasticsearch.streams
      .source(elasticsearch.config.topicIndex)
      .map(_.to[Topic])
  }

  def topicById(topicId: String): Future[Topic] = {
    execute {
      get(topicId).from(elasticsearch.config.topicIndex)
    }.map(_.result.to[Topic])
  }

}
