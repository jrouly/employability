package net.rouly.employability.ingest.scraping.backend

import java.util.UUID

import akka.NotUsed
import akka.stream.scaladsl._
import io.lemonlabs.uri._
import io.lemonlabs.uri.dsl._
import net.rouly.employability.blocking.BlockingExecutionContext
import net.rouly.employability.ingest.scraping.{JSoupClient, ScrapingBackend}
import net.rouly.employability.models.{CourseDescription, RawDocument}
import net.rouly.employability.streams.BookKeepingWireTap
import org.jsoup.nodes.Element

import scala.collection.JavaConverters._
import scala.concurrent.Future

class WoffordCollegeBackend(
  jsoup: JSoupClient
)(implicit ec: BlockingExecutionContext)
  extends ScrapingBackend {

  private val baseUrl = "http://catalog.wofford.edu"

  private val dataSet = "wofford"

  def scrape: Source[RawDocument, NotUsed] = {
    val courses = for {
      departmentUrls <- getDepartmentUrls
      courseElement <- Future.traverse(departmentUrls)(getCourses)
    } yield Source(courseElement.flatten.map(toDocument))

    Source
      .fromFutureSource(courses)
      .wireTap(BookKeepingWireTap(dataSet))
      .viaMat(Flow[RawDocument])(Keep.right)
  }

  private def getDepartmentUrls: Future[List[Url]] = {
    jsoup.get(baseUrl / "courses-instruction").map { document =>
      document
        .select("#atozindex")
        .select("li")
        .select("a")
        .asScala.toList
        .map(_.attr("href"))
        .map(path => baseUrl / path)
    }
  }

  private def getCourses(departmentUrl: Url): Future[List[Element]] = {
    jsoup.get(departmentUrl).map { document =>
      document
        .select("#coursestextcontainer")
        .select(".courseblock")
        .asScala.toList
    }
  }

  private def toDocument(element: Element): RawDocument = {
    val title = element.select(".courseblocktitle").text
    val desc = element.select(".courseblockdesc").text
    val uuid = title + desc + dataSet

    CourseDescription(
      id = UUID.nameUUIDFromBytes(uuid.getBytes),
      dataSet = dataSet,
      description = desc,
      title = Some(title)
    )
  }

}
