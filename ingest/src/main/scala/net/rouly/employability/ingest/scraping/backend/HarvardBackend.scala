package net.rouly.employability.ingest.scraping.backend

import java.util.UUID

import akka.NotUsed
import akka.stream.scaladsl._
import io.lemonlabs.uri.dsl._
import net.rouly.employability.blocking.BlockingExecutionContext
import net.rouly.employability.ingest.scraping.{JSoupClient, ScrapingBackend}
import net.rouly.employability.models.{CourseDescription, RawDocument}
import net.rouly.employability.streams.BookKeepingWireTap

import scala.collection.JavaConverters._
import scala.concurrent.Future

class HarvardBackend(
  jsoup: JSoupClient
)(implicit ec: BlockingExecutionContext)
  extends ScrapingBackend {

  private val baseUrl = "https://courses.harvard.edu/search"
  private val pages = 0 to 15525 by 25

  protected val dataSet = "harvard"

  def scrape: Source[RawDocument, NotUsed] = {
    val courses = Future.traverse(pages.toList)(getCoursesForPage)
      .map(_.flatten.toList)
      .map(Source.apply)

    Source
      .fromFutureSource(courses)
      .wireTap(BookKeepingWireTap(dataSet))
      .viaMat(Flow[RawDocument])(Keep.right)
  }

  protected def getCoursesForPage(page: Int): Future[List[CourseDescription]] = {
    val url = baseUrl ? s"start=$page"
    jsoup.get(url).map { document =>
      document
        .select("tr.course")
        .asScala
        .toList
        .flatMap { course =>
          val id = course.id().replaceAllLiterally("row_course_", "")
          val title = course.select(".course_title").select("#srl_title").text
          val desc = document.select(s"#moredetailstr-$id").select("#srl_description").text
          val uuid = title + desc + dataSet

          if (title.isEmpty || desc.isEmpty) None
          else Some {
            CourseDescription(
              id = UUID.nameUUIDFromBytes(uuid.getBytes),
              dataSet = dataSet,
              description = desc,
              title = Some(title)
            )
          }
        }
    }
  }

}
