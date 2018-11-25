package net.rouly.employability.ingest.scraping

import net.rouly.employability.blocking.BlockingExecutionContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

import scala.concurrent.Future

class JSoupClient(implicit ec: BlockingExecutionContext) {

  def get(url: String): Future[Document] = Future(Jsoup.connect(url).get())

}
