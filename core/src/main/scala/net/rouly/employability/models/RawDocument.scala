package net.rouly.employability.models

import java.util.UUID

import play.api.libs.json.{Format, JsResult, JsValue, Json}

sealed trait RawDocument {
  def id: UUID
  def dataSet: String
  def description: String
  def kind: String
  def title: Option[String]
}

case class JobPosting(
  id: UUID,
  dataSet: String,
  description: String,
  title: Option[String] = None
) extends RawDocument {

  override val kind: String = "job-posting"

}

object JobPosting {
  implicit val jobPostingFormat: Format[JobPosting] = Json.format[JobPosting]
}

case class CourseDescription(
  id: UUID,
  dataSet: String,
  description: String,
  title: Option[String] = None
) extends RawDocument {

  override val kind: String = "course-description"

}

object CourseDescription {
  implicit val courseDescriptionFormat: Format[CourseDescription] = Json.format[CourseDescription]
}

object RawDocument {
  implicit val format: Format[RawDocument] = new Format[RawDocument] {

    override def reads(json: JsValue): JsResult[RawDocument] = {
      val asJp = json.validate[JobPosting]
      val asCd = json.validate[CourseDescription]

      asJp orElse asCd
    }

    override def writes(o: RawDocument): JsValue = {
      o match {
        case jp: JobPosting => Json.toJson(jp)(JobPosting.jobPostingFormat)
        case cd: CourseDescription => Json.toJson(cd)(CourseDescription.courseDescriptionFormat)
      }
    }

  }
}
