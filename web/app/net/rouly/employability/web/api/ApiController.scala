package net.rouly.employability.web.api

import net.rouly.employability.models.Topic
import net.rouly.employability.web.elasticsearch.ElasticsearchWebService
import play.api.cache.Cached
import play.api.libs.json.{Format, Json}
import play.api.mvc.{AbstractController, ControllerComponents}

class ApiController(
  cc: ControllerComponents,
  cached: Cached,
  service: ElasticsearchWebService
) extends AbstractController(cc) {

  def allTopics = cached("api.allTopics") {
    Action(service.topicSource.chunkedResponse)
  }

  def topicById(id: String) = cached(s"api.topicById.$id") {
    Action {
      service
        .topicSource
        .filter(_.id == id)
        .take(1)
        .chunkedResponse
    }
  }

  def docsByTopicId(id: String) = cached(s"api.docsByTopicId.$id") {
    Action(service.documentsByTopic(id).chunkedResponse)
  }

}

object ApiController {

  implicit val topicFormat: Format[Topic] = Json.format[Topic]

}
