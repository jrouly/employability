package net.rouly.employability.preprocess.opennlp.reader

import akka.stream.Materializer
import akka.stream.scaladsl.StreamConverters
import com.softwaremill.macwire.wire
import net.rouly.common.config.Configuration
import net.rouly.employability.preprocess.opennlp.{OpenNlpModel, OpenNlpModelClient}
import play.api.libs.ws.StandaloneWSClient

import scala.concurrent.{ExecutionContext, Future}

class WSClientOpenNlpModelReader(
  configuration: Configuration,
  wsClient: StandaloneWSClient
)(implicit
  materializer: Materializer,
  ec: ExecutionContext
) extends OpenNlpModelReader {
  private val client = wire[OpenNlpModelClient]

  override def getModel(name: String): Future[OpenNlpModel] =
    client
      .requestModel(name)
      .map(_.bodyAsSource.runWith(StreamConverters.asInputStream()))
      .map(stream => new OpenNlpModel(name, stream))

}

