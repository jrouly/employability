package net.rouly.employability.ingest

import com.softwaremill.macwire.wire
import com.typesafe.scalalogging.StrictLogging
import net.rouly.employability.EmployabilityApp
import net.rouly.employability.elasticsearch.ElasticsearchModule
import net.rouly.employability.ingest.dataworld.DataWorldModule
import play.api.libs.ws.StandaloneWSClient
import play.api.libs.ws.ahc.StandaloneAhcWSClient

import scala.concurrent.Await
import scala.concurrent.duration._

object IngestApp
  extends App
  with EmployabilityApp
  with StrictLogging {

  val wsClient: StandaloneWSClient = StandaloneAhcWSClient()

  lazy val elasticsearch: ElasticsearchModule = new ElasticsearchModule(configuration)
  lazy val dataWorld: DataWorldModule = wire[DataWorldModule]
  lazy val streams: IngestStreams = wire[IngestStreams]

  logger.info("Start.")
  Await.result(streams.ingestGraph, 5.minutes)
  logger.info("Done.")

  Await.result(actorSystem.terminate(), 5.minutes)

  materializer.shutdown()
  elasticsearch.close()
  wsClient.close()

}
