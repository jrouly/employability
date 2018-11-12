package net.rouly.employability.preprocess

import akka.stream.scaladsl.Sink
import com.softwaremill.macwire.wire
import com.typesafe.scalalogging.StrictLogging
import net.rouly.employability.EmployabilityApp
import net.rouly.employability.elasticsearch.ElasticsearchModule
import net.rouly.employability.models.Document
import net.rouly.employability.postgres._
import net.rouly.employability.preprocess.opennlp.{AnalysisOpenNlpModels, OpenNlpModule}
import net.rouly.employability.preprocess.transform.{DocumentTransformFlow, PreProcessFlow}
import net.rouly.employability.streams.BookKeepingWireTap
import play.api.libs.ws.StandaloneWSClient
import play.api.libs.ws.ahc.StandaloneAhcWSClient

import scala.concurrent.Await
import scala.concurrent.duration._

object PreProcessApp
  extends App
  with EmployabilityApp
  with StrictLogging {

  val wsClient: StandaloneWSClient = StandaloneAhcWSClient()

  lazy val elasticsearch: ElasticsearchModule = wire[ElasticsearchModule]
  lazy val postgres: PostgresModule = wire[PostgresModule]
  lazy val opennlp: OpenNlpModule = wire[OpenNlpModule]

  // Blocking IO: Retrieve online binaries of opennlp models.
  val openNlpModels: AnalysisOpenNlpModels = new AnalysisOpenNlpModels(
    placeNameModel = Await.result(opennlp.reader.getModel("en-ner-location.bin"), 2.minutes),
    tokenizerModel = Await.result(opennlp.reader.getModel("en-token.bin"), 2.minutes)
  )

  // Set up DB schema.
  Await.result(postgres.init(), 5.seconds)

  lazy val graph = {
    import postgres.mapping._

    elasticsearch.streams
      .source(elasticsearch.config.jobPostingIndex)
      .wireTap(BookKeepingWireTap("elasticsearch"))
      .via(DocumentTransformFlow())
      .via(PreProcessFlow(openNlpModels))
      .alsoTo(postgres.streams.sink[Document[String]])
      .runWith(Sink.ignore)
  }

  logger.info("Start preprocessing.")
  Await.result(graph, 5.minutes)
  logger.info("Done.")

  Await.result(actorSystem.terminate(), 5.minutes)

  materializer.shutdown()
  elasticsearch.close()
  wsClient.close()
  postgres.close()

}
