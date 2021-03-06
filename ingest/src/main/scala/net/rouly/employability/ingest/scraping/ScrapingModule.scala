package net.rouly.employability.ingest.scraping

import akka.actor.ActorSystem
import akka.stream.scaladsl.Source
import com.softwaremill.macwire.{Module, wire}
import net.rouly.employability.blocking.BlockingExecutionContext
import net.rouly.employability.ingest.scraping.backend._
import net.rouly.employability.models.RawDocument
import net.rouly.employability.streams._

@Module
class ScrapingModule(implicit actorSystem: ActorSystem) {

  // For blocking execution.
  private implicit val blockingExecutionContext: BlockingExecutionContext = BlockingExecutionContext.byName("scraping-dispatcher")
  private lazy val jsoupClient: JSoupClient = wire[JSoupClient]

  private lazy val sources = List(
    wire[BerkeleyBackend].scrape,
    wire[BuffaloBackend].scrape,
    wire[CalPolyBackend].scrape,
    wire[CsuBackend].scrape,
    wire[GeorgeMasonBackend].scrape,
    wire[GeorgiaStateBackend].scrape,
    wire[HarvardBackend].scrape,
    wire[IllinoisBackend].scrape,
    wire[IowaStateBackend].scrape,
    wire[PennStateBackend].scrape,
    wire[StOlafBackend].scrape,
    wire[TexasAMBackend].scrape,
    wire[UAlabamaBackend].scrape,
    wire[UChicagoBackend].scrape,
    wire[UFloridaBackend].scrape,
    wire[UWashingtonBackend].scrape,
    wire[UncChapelHillBackend].scrape,
    wire[WoffordBackend].scrape
  )

  lazy val source: Source[RawDocument, _] = Source.multi(sources)

}
