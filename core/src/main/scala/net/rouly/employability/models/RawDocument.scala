package net.rouly.employability.models

import java.util.UUID

import play.api.libs.json._

sealed trait RawDocument {
  def id: UUID
  def dataSet: String
  def description: String
  def kind: DocumentKind
  def title: Option[String]
}

case class JobPosting(
  id: UUID,
  dataSet: String,
  description: String,
  title: Option[String] = None
) extends RawDocument {

  override val kind: DocumentKind = JobPosting.Kind

}

object JobPosting {
  final val Kind = DocumentKind.JobDescription
  implicit val reads: Reads[JobPosting] = Json.reads[JobPosting]
  implicit val owrites: OWrites[JobPosting] = Json.writes[JobPosting]
}

case class CourseDescription(
  id: UUID,
  dataSet: String,
  description: String,
  title: Option[String] = None
) extends RawDocument {

  override val kind: DocumentKind = CourseDescription.Kind

}

object CourseDescription {
  final val Kind = DocumentKind.CourseDescription
  implicit val reads: Reads[CourseDescription] = Json.reads[CourseDescription]
  implicit val owrites: OWrites[CourseDescription] = Json.writes[CourseDescription]
}

object RawDocument {
  implicit val format: Format[RawDocument] = new Format[RawDocument] {

    override def reads(json: JsValue): JsResult[RawDocument] = {
      (json \ "kind").validate[String] match {
        case JsSuccess(JobPosting.Kind.kind, _) => JobPosting.reads.reads(json)
        case JsSuccess(CourseDescription.Kind.kind, _) => CourseDescription.reads.reads(json)
        case _ => JsError("unrecognized 'kind'")
      }
    }

    override def writes(o: RawDocument): JsValue = {
      o match {
        case jp: JobPosting => JobPosting.owrites.writes(jp) ++ Json.obj("kind" -> JobPosting.Kind.kind)
        case cd: CourseDescription => CourseDescription.owrites.writes(cd) ++ Json.obj("kind" -> CourseDescription.Kind.kind)
      }
    }

  }
}
