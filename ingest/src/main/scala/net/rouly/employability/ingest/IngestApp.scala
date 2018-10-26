package net.rouly.employability.ingest

import akka.stream.scaladsl._
import com.softwaremill.macwire.wire
import com.typesafe.scalalogging.StrictLogging
import net.rouly.employability.EmployabilityApp
import net.rouly.employability.elasticsearch.ElasticsearchModule
import net.rouly.employability.ingest.dataworld.DataWorldModule
import net.rouly.employability.models.JobPosting
import net.rouly.employability.streams._
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

  val graph = {
    import elasticsearch.mapping._

    dataWorld.source
      .via(Flow.recordCountingFlow("elasticsearch"))
      .alsoTo(elasticsearch.streams.sink[JobPosting])
      .runWith(Sink.ignore)
  }

  logger.info("Start.")

  graph.onComplete { _ =>
    wsClient.close()
    elasticsearch.close()
    materializer.shutdown()
    actorSystem.terminate()
  }

  Await.result(graph, 5.minutes)

  logger.info("Done.")

}
