package net.rouly.employability.ingest.dataworld

import akka.stream.scaladsl._
import com.softwaremill.macwire.wire
import com.typesafe.scalalogging.StrictLogging
import net.rouly.common.config.Configuration
import net.rouly.employability.ingest.dataworld.csv.Extractor
import net.rouly.employability.ingest.models.JobPosting
import net.rouly.employability.ingest.streams._
import play.api.libs.ws.StandaloneWSClient

import scala.concurrent.ExecutionContext

class DataWorldModule(
  configuration: Configuration,
  wsClient: StandaloneWSClient
)(implicit ec: ExecutionContext) extends StrictLogging {

  private lazy val client: DataWorldClient = wire[DataWorldClient]
  private lazy val reader: DataWorldDataSetReader = wire[DataWorldDataSetReader]

  private lazy val dataSets = reader.all.map { dataset =>
    implicit val extractor: Extractor[JobPosting] = csv.jobPosting(dataset)
    logger.info(dataset.displayName)
    client.getCsv[JobPosting](dataset)
  }

  lazy val source: Source[JobPosting, _] = Source.multi(dataSets)

}
