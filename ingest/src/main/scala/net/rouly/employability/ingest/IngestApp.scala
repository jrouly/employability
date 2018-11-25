package net.rouly.employability.ingest

import akka.Done
import akka.stream.scaladsl.Sink
import com.softwaremill.macwire.wire
import com.typesafe.scalalogging.StrictLogging
import net.rouly.employability.EmployabilityApp
import net.rouly.employability.elasticsearch.ElasticsearchModule
import net.rouly.employability.ingest.dataworld.DataWorldModule
import net.rouly.employability.ingest.scraping.ScrapingModule
import net.rouly.employability.models.RawDocument
import net.rouly.employability.streams._
import play.api.libs.ws.StandaloneWSClient
import play.api.libs.ws.ahc.StandaloneAhcWSClient

import scala.concurrent.Future

object IngestApp
  extends App
  with EmployabilityApp
  with StrictLogging {

  val wsClient: StandaloneWSClient = StandaloneAhcWSClient()

  lazy val elasticsearch: ElasticsearchModule = new ElasticsearchModule(configuration)
  lazy val dataWorld: DataWorldModule = wire[DataWorldModule]
  lazy val scraping: ScrapingModule = wire[ScrapingModule]

  lazy val ingestGraph: Future[Done] = {
    import elasticsearch.mapping._

    val source = dataWorld.source merge scraping.source

    source
      .alsoTo(elasticsearch.streams.sink[RawDocument])
      .wireTap(BookKeepingWireTap("elasticsearch"))
      .runWith(Sink.ignore)
  }

  // Register shutdown hooks.
  actorSystem.registerOnTermination {
    elasticsearch.close()
    wsClient.close()
    materializer.shutdown()
  }

  // Execute the application.
  run(ingestGraph)

}
