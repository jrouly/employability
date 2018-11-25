package net.rouly.employability.ingest.scraping.backend

import akka.NotUsed
import akka.stream.scaladsl.{Flow, Keep, Source}
import io.lemonlabs.uri.Url
import net.rouly.employability.blocking.BlockingExecutionContext
import net.rouly.employability.ingest.scraping.{JSoupClient, ScrapingBackend}
import net.rouly.employability.models.{CourseDescription, RawDocument}
import net.rouly.employability.streams.BookKeepingWireTap
import org.jsoup.nodes.Element

import scala.concurrent.Future

abstract class GenericBackend(jsoup: JSoupClient)(implicit ec: BlockingExecutionContext) extends ScrapingBackend {

  protected def dataSet: String

  override def scrape: Source[RawDocument, NotUsed] = {
    val courses = for {
      departmentUrls <- getDepartmentUrls
      courses <- Future.traverse(departmentUrls)(getCourses)
    } yield Source(courses.flatten)

    Source
      .fromFutureSource(courses)
      .wireTap(BookKeepingWireTap(dataSet))
      .viaMat(Flow[RawDocument])(Keep.right)
  }

  protected def getDepartmentUrls: Future[List[Url]]

  protected def getCourses(departmentUrl: Url): Future[List[CourseDescription]]

  protected def toDocument(element: Element): CourseDescription

}
