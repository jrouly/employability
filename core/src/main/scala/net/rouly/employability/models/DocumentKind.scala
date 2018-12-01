package net.rouly.employability.models

sealed abstract class DocumentKind(val kind: String)

object DocumentKind {
  case object JobDescription extends DocumentKind("job-description")
  case object CourseDescription extends DocumentKind("course-description")
}
