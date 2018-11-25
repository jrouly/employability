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

class StOlafBackend(
  jsoup: JSoupClient
)(implicit ec: BlockingExecutionContext)
  extends GenericBackend(jsoup) {

  private val baseUrl = "http://catalog.stolaf.edu"

  protected val dataSet = "stolaf"

  protected def getDepartmentUrls: Future[List[Url]] = {
    jsoup.get(baseUrl / "academic-programs").map { document =>
      document
        .select("ul[id~=academic-programs]")
        .select("li")
        .select("a")
        .asScala.toList
        .map(_.attr("href"))
        .map(path => baseUrl / path)
    }
  }

  protected def getCourses(departmentUrl: Url): Future[List[CourseDescription]] = {
    jsoup.get(departmentUrl).map { document =>
      document
        .select("#coursestextcontainer")
        .select(".courseblock")
        .asScala.toList
        .map(toDocument)
    }
  }

  protected def toDocument(element: Element): CourseDescription = {
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
