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

class IowaStateBackend(
  jsoup: JSoupClient
)(implicit ec: BlockingExecutionContext)
  extends GenericBackend(jsoup) {

  private val baseUrl = "http://catalog.iastate.edu"

  override protected val dataSet = "iastate"

  protected def getDepartmentUrls: Future[List[Url]] = {
    jsoup.get(baseUrl / "azcourses").map { document =>
      document
        .select("#atozindex")
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
        .select("#courseinventorycontainer")
        .select(".courseblock")
        .asScala.toList
        .flatMap(toDocument)
    }
  }

  protected def toDocument(element: Element): Option[CourseDescription] = {
    val title = element.select(".courseblocktitle").text
    val desc = element.select(".courseblockdesc").text
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
