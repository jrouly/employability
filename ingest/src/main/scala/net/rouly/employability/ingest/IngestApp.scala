package net.rouly.employability.ingest

import akka.Done
import akka.stream.scaladsl.Sink
import com.softwaremill.macwire.wire
import com.typesafe.scalalogging.StrictLogging
import net.rouly.employability.EmployabilityApp
import net.rouly.employability.elasticsearch.ElasticsearchModule
import net.rouly.employability.ingest.dataworld.DataWorldModule
import net.rouly.employability.models.JobPosting
import net.rouly.employability.streams.BookKeepingWireTap
import play.api.libs.ws.StandaloneWSClient
import play.api.libs.ws.ahc.StandaloneAhcWSClient

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

object IngestApp
  extends App
  with EmployabilityApp
  with StrictLogging {

  val wsClient: StandaloneWSClient = StandaloneAhcWSClient()

  lazy val elasticsearch: ElasticsearchModule = new ElasticsearchModule(configuration)
  lazy val dataWorld: DataWorldModule = wire[DataWorldModule]

  lazy val ingestGraph: Future[Done] = {
    import elasticsearch.mapping._

    dataWorld.source
      .alsoTo(elasticsearch.streams.sink[JobPosting])
      .wireTap(BookKeepingWireTap("elasticsearch"))
      .runWith(Sink.ignore)
  }

  logger.info("Start.")
  Await.result(ingestGraph, 5.minutes)
  logger.info("Done.")

  Await.result(actorSystem.terminate(), 5.minutes)

  materializer.shutdown()
  elasticsearch.close()
  wsClient.close()
}
