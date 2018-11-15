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
      topic <- service.topicSource.filter(_.id == id).runWith(Sink.headOption)
      docs <- service.documentsByTopic(id).take(10).runWith(Sink.collection)
    } yield {
      topic.render(application.topic(_, docs.toList))
    }
  }

  def docsByTopicId(id: String) = Action.async {
    for {
      docs <- service.documentsByTopic(id).take(10).runWith(Sink.collection)
    } yield Ok(docs.toString)
  }

  def allDocuments = Action.async {
    for {
      docs <- service.documentSource.take(5).runWith(Sink.collection)
    } yield Ok(application.documents(docs.toList))
  }

  def docById(id: String) = Action.async {
    for {
      doc <- service.documentSource.filter(_.id == id).runWith(Sink.headOption)
    } yield doc.render(application.document.apply)
  }

}
