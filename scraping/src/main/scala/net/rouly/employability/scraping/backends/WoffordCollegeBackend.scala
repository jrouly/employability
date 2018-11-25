package net.rouly.employability.scraping.backends

import java.util.UUID

import akka.NotUsed
import akka.stream.scaladsl.Source
import io.lemonlabs.uri.Url
import io.lemonlabs.uri.dsl._
import net.rouly.employability.models.RawDocument
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

import scala.collection.JavaConverters._

class WoffordCollegeBackend extends Backend {

  private val baseUrl = "http://catalog.wofford.edu"

  private val dataSet = "wofford"

  def scrape: Source[RawDocument, NotUsed] = {
    val courses = for {
      departmentUrl <- getDepartmentUrls
      courseElement <- getCourses(departmentUrl)
    } yield toDocument(courseElement)

    Source(courses)
  }

  private def getDepartmentUrls: List[Url] = {
    Jsoup
      .connect(baseUrl / "courses-instruction")
      .get()
      .select("#atozindex")
      .select("li")
      .select("a")
      .asScala.toList
      .map(_.attr("href"))
      .map(path => baseUrl / path)
  }

  private def getCourses(departmentUrl: Url): List[Element] = {
    Jsoup
      .connect(departmentUrl)
      .get()
      .select("#coursestextcontainer")
      .select(".courseblock")
      .asScala.toList
  }

  private def toDocument(element: Element): RawDocument = {
    val title = element.select(".courseblocktitle").text
    val desc = element.select(".courseblockdesc").text

    RawDocument(
      id = UUID.nameUUIDFromBytes(title.getBytes),
      dataSet = dataSet,
      description = desc,
      kind = "course-description",
      title = Some(title)
    )
  }

}
