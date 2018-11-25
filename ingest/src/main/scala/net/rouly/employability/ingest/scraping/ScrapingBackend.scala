package net.rouly.employability.ingest.scraping

import akka.NotUsed
import akka.stream.scaladsl.Source
import net.rouly.employability.models.RawDocument

trait ScrapingBackend {

  def scrape: Source[RawDocument, NotUsed]

}
