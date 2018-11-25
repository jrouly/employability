package net.rouly.employability.ingest.scraping.backend

import java.util.UUID

import akka.NotUsed
import akka.stream.scaladsl.{Flow, Keep, Source}
import io.lemonlabs.uri._
import io.lemonlabs.uri.dsl._
import net.rouly.employability.blocking.BlockingExecutionContext
import net.rouly.employability.ingest.scraping.{JSoupClient, ScrapingBackend}
import net.rouly.employability.models.{CourseDescription, RawDocument}
import net.rouly.employability.streams.BookKeepingWireTap
import org.jsoup.nodes.Element

import scala.collection.JavaConverters._
import scala.concurrent.Future

class BerkeleyBackend(
  jsoup: JSoupClient
)(implicit ec: BlockingExecutionContext)
  extends ScrapingBackend {

  private val baseUrl = "http://guide.berkeley.edu"

  private val dataSet = "ucberkeley"

  def scrape: Source[RawDocument, NotUsed] = {
    val courses = for {
      departmentUrls <- getDepartmentUrls
      courses <- Future.traverse(departmentUrls)(getCourses)
    } yield Source(courses.flatten)

    Source
      .fromFutureSource(courses)
      .wireTap(BookKeepingWireTap(dataSet))
      .viaMat(Flow[RawDocument])(Keep.right)
  }

  private def getDepartmentUrls: Future[List[Url]] = {
    jsoup.get(baseUrl / "courses").map { document =>
      document
        .select("#atozindex")
        .select("li")
        .select("a")
        .asScala.toList
        .map(_.attr("href"))
        .map(path => baseUrl / path)
    }
  }

  private def getCourses(departmentUrl: Url): Future[List[CourseDescription]] = {
    jsoup.get(departmentUrl).map { document =>
      document
        .select(".courseblock")
        .asScala.toList
        .map(toDocument)
    }
  }

  private def toDocument(element: Element): CourseDescription = {
    val titleEl = element.select(".courseblocktitle")
    val bodyEl = element.select(".coursebody")

    val code = titleEl.select(".code").text
    val title = titleEl.select(".title").text
    val desc = bodyEl.select(".courseblockdesc").text
    val uuid = code + title + desc + dataSet

    CourseDescription(
      id = UUID.nameUUIDFromBytes(uuid.getBytes),
      dataSet = dataSet,
      description = desc,
      title = Some(title)
    )
  }

}
