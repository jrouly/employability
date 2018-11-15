package net.rouly.employability.web.api

import net.rouly.employability.models.Topic
import net.rouly.employability.web.elasticsearch.ElasticsearchService
import play.api.libs.json.{Format, Json}
import play.api.mvc.{AbstractController, ControllerComponents}

class ApiController(
  cc: ControllerComponents,
  service: ElasticsearchService
) extends AbstractController(cc) {

  def allTopics = Action(service.topicSource.chunkedResponse)

  def topicById(id: String) = Action {
    service
      .topicSource
      .filter(_.id == id)
      .take(1)
      .chunkedResponse
  }

  def docsByTopicId(id: String) = Action(service.documentsByTopic(id).chunkedResponse)

}

object ApiController {

  implicit val topicFormat: Format[Topic] = Json.format[Topic]

}
