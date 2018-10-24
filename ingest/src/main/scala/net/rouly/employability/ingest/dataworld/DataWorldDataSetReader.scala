package net.rouly.employability.ingest.dataworld

import java.io.{File, FilenameFilter}

import com.typesafe.scalalogging.StrictLogging
import net.rouly.employability.ingest.dataworld.DataWorldDataSetReader._
import net.rouly.employability.ingest.dataworld.model.DataWorldDataSet
import play.api.libs.json.{Format, Json}

import scala.io.Source

class DataWorldDataSetReader extends StrictLogging {

  lazy val all: List[DataWorldDataSet] = {
    val files = readFiles
    files.foreach { dataset => logger.info(s"Loaded dataset ${dataset.displayName}") }
    files
  }

  private def readFiles: List[DataWorldDataSet] = {
    val path = getClass.getResource("/datasets/jobs")
    val folder = new File(path.getPath)
    if (folder.exists() && folder.isDirectory)
      folder
        .listFiles(JsonFilenameFilter)
        .map(readFile)
        .toList
        .flatMap {
          case Left(error) =>
            logger.warn(error)
            None
          case Right(dataSet) =>
            Some(dataSet)
        }
    else Nil
  }

  private def readFile(file: File): Either[String, DataWorldDataSet] = {
    val contents = Source.fromFile(file).getLines().mkString
    Json
      .parse(contents)
      .validate[DataWorldDataSet](dataSetFormat)
      .asEither
      .left.map { errors => s"Unable to parse ${file.getName} as a valid DataWorldDataSet. errors=$errors" }
  }

}

object DataWorldDataSetReader {
  private object JsonFilenameFilter extends FilenameFilter {
    override def accept(dir: File, name: String): Boolean = name.endsWith(".json")
  }

  private implicit val dataSetFormat: Format[DataWorldDataSet] = Json.format[DataWorldDataSet]
}
