package net.rouly.employability.preprocess.opennlp

import akka.stream.Materializer
import com.softwaremill.macwire.{Module, wire}
import net.rouly.common.config.Configuration
import net.rouly.employability.preprocess.opennlp.reader._
import opennlp.tools.langdetect.LanguageDetectorModel
import opennlp.tools.tokenize.TokenizerModel
import play.api.libs.ws.StandaloneWSClient

import scala.concurrent.{ExecutionContext, Future}

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

  val download: Future[AnalysisOpenNlpModels] = {
    for {
      languageModel <- reader.getModel("langdetect-183.bin", nlpModelDownload)
      tokenizerModel <- reader.getModel("en-token.bin", sourceForge)
    } yield new AnalysisOpenNlpModels(
      languageDetector = new LanguageDetectorModel(languageModel.stream),
      tokenizerModel = new TokenizerModel(tokenizerModel.stream)
    )
  }

}

