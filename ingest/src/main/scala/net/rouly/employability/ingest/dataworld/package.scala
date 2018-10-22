package net.rouly.employability.ingest

import java.util.UUID

import net.rouly.employability.ingest.models.JobPosting

import scala.util.Try

package object dataworld {

  object csv {

    type Extractor[T] = Map[String, String] => Try[T]

    /**
      * Build a [[JobPosting]] from a CSV map.
      */
    def jobPosting(
      dataSet: String,
      descriptionColumn: String,
      titleColumn: String,
      skillsColumn: Option[String] = None
    ): Extractor[JobPosting] = data => Try(JobPosting(
      id = UUID.nameUUIDFromBytes(data.values.mkString.getBytes),
      dataSet = dataSet,
      description = data(descriptionColumn),
      title = data(titleColumn).emptyToNone,
      skills = skillsColumn.flatMap(data(_).emptyToNone)
    ))

  }

  private implicit class RichString(string: String) {
    private val trim = string.trim()
    def emptyToNone: Option[String] = if (trim.isEmpty) None else Some(trim)
  }

}
