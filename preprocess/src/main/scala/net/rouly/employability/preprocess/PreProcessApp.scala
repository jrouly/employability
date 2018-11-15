package net.rouly.employability.preprocess

import akka.stream.scaladsl.Sink
import com.softwaremill.macwire.wire
import com.typesafe.scalalogging.StrictLogging
import net.rouly.employability.EmployabilityApp
import net.rouly.employability.elasticsearch.ElasticsearchModule
import net.rouly.employability.models.Document
import net.rouly.employability.postgres._
import net.rouly.employability.preprocess.opennlp.OpenNlpModule
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
  val openNlpModels = Await.result(opennlp.download, 2.minutes)

  // Set up DB schema.
  Await.result(postgres.init(), 5.seconds)

  lazy val graph = {
    import postgres.mapping._
    val sink = Sink.foreachAsync(parallelism)(postgres.insert[Document[String]])

    elasticsearch.streams
      .source(elasticsearch.config.jobPostingIndex)
      .async
      .wireTap(BookKeepingWireTap("elasticsearch"))
      .via(DocumentTransformFlow())
      .via(PreProcessFlow(openNlpModels))
      .wireTap(BookKeepingWireTap("preprocessed"))
      .runWith(sink)
  }

  logger.info("Start preprocessing.")
  Await.result(graph, 10.minutes)
  logger.info("Done.")

  Await.result(actorSystem.terminate(), 5.minutes)

  materializer.shutdown()
  elasticsearch.close()
  wsClient.close()
  postgres.close()

}
