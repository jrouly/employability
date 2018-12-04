package net.rouly.employability.models

import play.api.libs.json.{Format, JsResult, JsString, JsValue}

sealed abstract class DocumentKind(val kind: String)

object DocumentKind {
  case object JobDescription extends DocumentKind("job-description")
  case object CourseDescription extends DocumentKind("course-description")

  def apply(string: String): DocumentKind = {
    List(JobDescription, CourseDescription).find(_.kind == string).get
  }

  implicit val format: Format[DocumentKind] = new Format[DocumentKind] {
    override def writes(o: DocumentKind): JsValue = JsString(o.kind)
    override def reads(json: JsValue): JsResult[DocumentKind] = json.validate[String].map(DocumentKind.apply)
  }
}
