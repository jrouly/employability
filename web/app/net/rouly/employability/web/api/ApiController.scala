package net.rouly.employability.web.api

import net.rouly.employability.models.Topic
import net.rouly.employability.web.elasticsearch.{DocumentService, TopicService}
import play.api.libs.json.{Format, Json}
import play.api.mvc.{AbstractController, ControllerComponents}

class ApiController(
  cc: ControllerComponents,
  documentService: DocumentService,
  topicService: TopicService
) extends AbstractController(cc) {

  def allTopics = {
    Action(topicService.topicSource.chunkedResponse)
  }

  def topicById(id: String) = {
    Action {
      topicService
        .topicSource
        .filter(_.id == id)
        .take(1)
        .chunkedResponse
    }
  }

  def docsByTopicId(id: String) = {
    Action(documentService.documentsByTopic(id).chunkedResponse)
  }

}

object ApiController {

  implicit val topicFormat: Format[Topic] = Json.format[Topic]

}
