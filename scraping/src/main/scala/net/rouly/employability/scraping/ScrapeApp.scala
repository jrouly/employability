package net.rouly.employability.scraping

import akka.Done
import akka.stream.scaladsl.Sink
import com.softwaremill.macwire.wire
import com.typesafe.scalalogging.StrictLogging
import net.rouly.employability.EmployabilityApp
import net.rouly.employability.elasticsearch.ElasticsearchModule
import net.rouly.employability.models.RawDocument
import net.rouly.employability.scraping.backends.BackendModule
import net.rouly.employability.streams.BookKeepingWireTap

import scala.concurrent.Future

object ScrapeApp extends App
  with EmployabilityApp
  with StrictLogging {

  lazy val backends: BackendModule = wire[BackendModule]
  lazy val elasticsearch: ElasticsearchModule = new ElasticsearchModule(configuration)

  lazy val scrapeGraph: Future[Done] = {
    import elasticsearch.mapping._

    backends.source
      .alsoTo(elasticsearch.streams.sink[RawDocument])
      .wireTap(BookKeepingWireTap("elasticsearch"))
      .runWith(Sink.ignore)
  }

  // Register shutdown hooks.
  actorSystem.registerOnTermination {
    elasticsearch.close()
    materializer.shutdown()
  }

  // Execute the application.
  run(scrapeGraph)

}
