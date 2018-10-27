package net.rouly.employability.analysis.opennlp

import akka.stream.Materializer
import com.softwaremill.macwire.{Module, wire}
import net.rouly.common.config.Configuration
import net.rouly.employability.analysis.opennlp.reader.{CachingOpenNlpModelReader, OpenNlpModelReader, WSClientOpenNlpModelReader}
import play.api.libs.ws.StandaloneWSClient

import scala.concurrent.ExecutionContext

@Module
class OpenNlpModule(
  configuration: Configuration,
  wsClient: StandaloneWSClient
)(implicit materializer: Materializer, ec: ExecutionContext) {

  lazy val reader: OpenNlpModelReader = {
    val wsReader: OpenNlpModelReader = wire[WSClientOpenNlpModelReader]
    wire[CachingOpenNlpModelReader]
  }

}

