package net.rouly.employability.scraping.backends

import akka.stream.scaladsl.Source
import com.softwaremill.macwire.{Module, wire}
import net.rouly.employability.models.RawDocument
import net.rouly.employability.streams._

@Module
class BackendModule {

  private lazy val gmu = wire[GeorgeMasonUniversityBackend]
  private lazy val wofford = wire[WoffordCollegeBackend]

  private lazy val sources = List(
    gmu.scrape,
    wofford.scrape
  )

  lazy val source: Source[RawDocument, _] = Source.multi(sources)

}
