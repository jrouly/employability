package net.rouly.employability.ingest.scraping.backend

import java.util.UUID

import io.lemonlabs.uri._
import io.lemonlabs.uri.dsl._
import net.rouly.employability.blocking.BlockingExecutionContext
import net.rouly.employability.ingest.scraping.JSoupClient
import net.rouly.employability.models.CourseDescription
import org.jsoup.nodes.Element

import scala.collection.JavaConverters._
import scala.concurrent.Future

class GeorgiaStateBackend(
  jsoup: JSoupClient
)(implicit ec: BlockingExecutionContext)
  extends GenericBackend(jsoup) {

  private val baseUrl = "https://catalog.gsu.edu/undergraduate20182019"

  override protected val dataSet = "Georgia State University"

  protected def getDepartmentUrls: Future[List[Url]] = {
    jsoup.get(baseUrl / "course-descriptions").map { document =>
      document
        .select("#course-subjects")
        .select("p")
        .select("a[href]")
        .asScala.toList
        .map(_.attr("href"))
        .map(_.drop(3)) // They are all prefixed with "../"
        .map(path => baseUrl / path)
    }
  }

  protected def getCourses(departmentUrl: Url): Future[List[CourseDescription]] = {
    jsoup.get(departmentUrl).map { document =>
      document
        .select(".course_description")
        .asScala.toList
        .flatMap(toDocument)
    }
  }.recover { case _ => Nil }

  protected def toDocument(element: Element): Option[CourseDescription] = {

    val title = element.selectFirst("tr").select("th").next("th").text

    val descLabelTd = element.select("td.course-label:contains(Description)")
    val desc = descLabelTd.next("td").text

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
