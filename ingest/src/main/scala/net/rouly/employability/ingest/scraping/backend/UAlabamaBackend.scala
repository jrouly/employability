package net.rouly.employability.ingest.scraping.backend

import java.util.UUID

import akka.NotUsed
import akka.stream.scaladsl.{Flow, Keep, Source}
import io.lemonlabs.uri.dsl._
import net.rouly.employability.blocking.BlockingExecutionContext
import net.rouly.employability.ingest.scraping.{JSoupClient, ScrapingBackend}
import net.rouly.employability.models.{CourseDescription, RawDocument}
import net.rouly.employability.streams.BookKeepingWireTap
import org.jsoup.nodes.Element

import scala.collection.JavaConverters._
import scala.concurrent.Future

class UAlabamaBackend(
  jsoup: JSoupClient
)(implicit ec: BlockingExecutionContext)
  extends ScrapingBackend {

  private val baseUrl = "https://catalog.ua.edu"

  protected val dataSet = "University of Alabama"

  override def scrape: Source[RawDocument, NotUsed] = {
    val courses = for {
      collegeUrls <- getCollegeUrls
      departmentUrls <- Future.traverse(collegeUrls)(getDepartmentUrls)
      courses <- Future.traverse(departmentUrls.flatten)(getCourses)
    } yield Source(courses.flatten)

    Source
      .fromFutureSource(courses)
      .wireTap(BookKeepingWireTap(dataSet))
      .viaMat(Flow[RawDocument])(Keep.right)
  }

  protected def getCollegeUrls: Future[List[String]] = {
    jsoup.get(baseUrl / "undergraduate").map { document =>
      document
        .select("ul[id~=undergraduate]")
        .select("li")
        .select("a[href]")
        .asScala.toList
        .filter(_.text.contains("College of"))
        .map(_.attr("href"))
        .map(path => baseUrl / path)
        .map(_.toString)
    }
  }

  protected def getDepartmentUrls(collegeUrl: String): Future[List[String]] = {
    jsoup.get(collegeUrl).map { document =>
      document
        .select(".dept-listing")
        .select("li")
        .select("a[href]")
        .asScala.toList
        .map(_.attr("href"))
        .map(path => baseUrl / path / "courses")
        .map(_.toString)
    }
  }

  protected def getCourses(departmentUrl: String): Future[List[CourseDescription]] = {
    jsoup.get(departmentUrl).map { document =>
      document
        .select(".courseblock")
        .asScala.toList
        .flatMap(toCourse)
    }.recover { case _ => Nil }
  }

  protected def toCourse(element: Element): Option[CourseDescription] = {
    val title = element.select(".cb_title").text
    val desc = element.select(".cb_desc").text
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
