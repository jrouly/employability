package net.rouly.employability.web.api

import net.rouly.employability.models.Topic
import net.rouly.employability.web.elasticsearch.{DocumentService, TopicService}
import play.api.cache.Cached
import play.api.libs.json.{Format, Json}
import play.api.mvc.{AbstractController, ControllerComponents}

class ApiController(
  cc: ControllerComponents,
  cached: Cached,
  documentService: DocumentService,
  topicService: TopicService
) extends AbstractController(cc) {

  def allTopics = cached("api.allTopics") {
    Action(topicService.topicSource.chunkedResponse)
  }

  def topicById(id: String) = cached(s"api.topicById.$id") {
    Action {
      topicService
        .topicSource
        .filter(_.id == id)
        .take(1)
        .chunkedResponse
    }
  }

  def docsByTopicId(id: String) = cached(s"api.docsByTopicId.$id") {
    Action(documentService.documentsByTopic(id).chunkedResponse)
  }

}

object ApiController {

  implicit val topicFormat: Format[Topic] = Json.format[Topic]

}
