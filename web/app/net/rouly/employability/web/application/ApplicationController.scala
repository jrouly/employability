package net.rouly.employability.web.application

import akka.stream.Materializer
import akka.stream.scaladsl.Sink
import net.rouly.employability.web.elasticsearch.ElasticsearchService
import play.api.mvc.{AbstractController, ControllerComponents}
import views.html.application

import scala.concurrent.ExecutionContext

class ApplicationController(
  cc: ControllerComponents,
  service: ElasticsearchService
)(implicit mat: Materializer, ec: ExecutionContext) extends AbstractController(cc) {

  def index = Action(Ok(application.index()))

  def allTopics = Action.async {
    for {
      topics <- service.topicSource.runWith(Sink.collection)
    } yield Ok(application.topics(topics.toList.sortBy(_.id.toInt)))
  }

  def topicById(id: String) = Action.async {
    for {
      topic <- service.topicSource.filter(_.id == id).runWith(Sink.head)
    } yield Ok(application.components.topic(topic))
  }

  def docsByTopicId(id: String) = Action.async {
    for {
      topic <- service.documentsByTopic(id).take(10).runWith(Sink.collection)
    } yield Ok(topic.toString)
  }

}
