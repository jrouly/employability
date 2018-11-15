package net.rouly.employability.preprocess.opennlp

import akka.stream.Materializer
import com.softwaremill.macwire.{Module, wire}
import net.rouly.common.config.Configuration
import net.rouly.employability.preprocess.opennlp.reader._
import play.api.libs.ws.StandaloneWSClient

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext}

@Module
class OpenNlpModule(
  configuration: Configuration,
  wsClient: StandaloneWSClient
)(implicit materializer: Materializer, ec: ExecutionContext) {

  private lazy val reader: OpenNlpModelReader = {
    val wsReader: OpenNlpModelReader = wire[WSClientOpenNlpModelReader]
    wire[CachingOpenNlpModelReader]
  }

  private val nlpModelDownload = "http://artfiles.org/apache.org/opennlp/models/langdetect/1.8.3"
  private val sourceForge = "http://opennlp.sourceforge.net/models-1.5"

  def download(): AnalysisOpenNlpModels = {
    new AnalysisOpenNlpModels(
      languageDetector = Await.result(reader.getModel("langdetect-183.bin", nlpModelDownload), 2.minutes),
      placeNameModel = Await.result(reader.getModel("en-ner-location.bin", sourceForge), 2.minutes),
      tokenizerModel = Await.result(reader.getModel("en-token.bin", sourceForge), 2.minutes)
    )
  }
}

