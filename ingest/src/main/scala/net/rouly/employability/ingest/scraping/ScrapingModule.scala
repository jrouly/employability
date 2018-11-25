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

  private lazy val berkeley: BerkeleyBackend = wire[BerkeleyBackend]
  private lazy val gmu: GeorgeMasonBackend = wire[GeorgeMasonBackend]
  private lazy val stolaf: StOlafBackend = wire[StOlafBackend]
  private lazy val uchicago: UChicagoBackend = wire[UChicagoBackend]
  private lazy val wofford: WoffordBackend = wire[WoffordBackend]

  private lazy val sources = List(
    berkeley.scrape,
    gmu.scrape,
    stolaf.scrape,
    uchicago.scrape,
    wofford.scrape
  )

  lazy val source: Source[RawDocument, _] = Source.multi(sources)

}
