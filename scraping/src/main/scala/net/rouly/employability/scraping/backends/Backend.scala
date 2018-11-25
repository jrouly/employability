package net.rouly.employability.scraping.backends

import akka.NotUsed
import akka.stream.scaladsl.Source
import net.rouly.employability.models.RawDocument

trait Backend {

  def scrape: Source[RawDocument, NotUsed]

}
