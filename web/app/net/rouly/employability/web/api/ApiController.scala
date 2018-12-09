package net.rouly.employability.web.api

import net.rouly.common.server.play.implicits.ResultBodyImplicits._
import net.rouly.employability.models.Topic
import net.rouly.employability.web.elasticsearch.{DocumentService, TopicService}
import play.api.libs.json.{Format, Json}
import play.api.mvc.{AbstractController, ControllerComponents}

import scala.concurrent.ExecutionContext

class ApiController(
  cc: ControllerComponents,
  documentService: DocumentService,
  topicService: TopicService
)(implicit ec: ExecutionContext)
  extends AbstractController(cc) {

  def allTopics = {
    Action(topicService.topicSource.chunkedResponse)
  }

  def topicById(id: String) = {
    Action.async {
      topicService
        .topicById(id)
        .toJsonResult
    }
  }

  def docsByTopicId(id: String) = {
    Action.async {
      documentService
        .documentById(id)
        .toJsonResult
    }
  }

}

object ApiController {

  implicit val topicFormat: Format[Topic] = Json.format[Topic]

}
