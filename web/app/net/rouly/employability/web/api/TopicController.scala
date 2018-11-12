package net.rouly.employability.web.api

import net.rouly.employability.models.Topic
import net.rouly.employability.web.api.TopicController.topicFormat
import net.rouly.employability.web.elasticsearch.TopicService
import play.api.libs.json.{Format, Json}
import play.api.mvc.{AbstractController, ControllerComponents}

class TopicController(
  cc: ControllerComponents,
  topicService: TopicService
) extends AbstractController(cc) {

  def allTopics = Action {
    val stream = topicService
      .topicSource
      .map(Json.toJson(_))
    Ok.chunked(stream).as("application/json")
  }

  def topicById(id: String) = Action {
    val stream = topicService
      .topicSource
      .filter(_.id == id)
      .take(1)
      .map(Json.toJson(_))
    Ok.chunked(stream).as("application/json")
  }

}

object TopicController {

  implicit val topicFormat: Format[Topic] = Json.format[Topic]

}
