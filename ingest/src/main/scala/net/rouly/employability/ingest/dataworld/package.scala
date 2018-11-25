package net.rouly.employability.ingest

import java.util.UUID

import net.rouly.employability.ingest.dataworld.model.DataWorldDataSet
import net.rouly.employability.models.{JobPosting, RawDocument}

import scala.util.Try

package object dataworld {

  object csv {

    type Extractor[T] = Map[String, String] => Try[T]

    /**
      * Build a [[RawDocument]] from a CSV map.
      */
    def jobPosting(dataSet: DataWorldDataSet): Extractor[RawDocument] = {
      record =>
        Try(JobPosting(
          id = UUID.nameUUIDFromBytes(record.values.mkString.getBytes),
          dataSet = dataSet.displayName,
          description = record(dataSet.dictionary("description")),
          title = record(dataSet.dictionary("title")).emptyToNone
        ))
    }

  }

  private implicit class RichString(string: String) {
    private val trim = string.trim()
    def emptyToNone: Option[String] = if (trim.isEmpty) None else Some(trim)
  }

}
