package net.rouly.employability.preprocess.opennlp

import net.rouly.common.config.Configuration
import play.api.libs.ws.{StandaloneWSClient, StandaloneWSResponse}

import scala.concurrent.Future
import scala.concurrent.duration._

class OpenNlpModelClient(
  configuration: Configuration,
  wsClient: StandaloneWSClient
) {

  def requestModel(baseUrl: String, name: String): Future[StandaloneWSResponse] = wsClient
    .url(s"$baseUrl/$name")
    .withFollowRedirects(true)
    .withRequestTimeout(5.minutes)
    .stream()

}
