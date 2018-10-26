package net.rouly.employability.ingest.dataworld

import akka.stream.alpakka.csv.scaladsl.{CsvParsing, CsvToMap}
import akka.stream.scaladsl.{Flow, Source}
import com.typesafe.scalalogging.StrictLogging
import net.rouly.common.config.Configuration
import net.rouly.employability.ingest.dataworld.model.DataWorldDataSet
import net.rouly.employability.ingest.streams._
import play.api.libs.ws.{StandaloneWSClient, StandaloneWSResponse}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

private[dataworld] class DataWorldClient(
  configuration: Configuration,
  wsClient: StandaloneWSClient
)(implicit ec: ExecutionContext)
  extends StrictLogging {

  private val baseUrl: String = configuration.get("data.world.baseurl", "https://query.data.world/s")

  /**
    * Retrieve a raw data.world dataset.
    */
  def getDataSet(dataSet: DataWorldDataSet): Future[StandaloneWSResponse] = wsClient
    .url(s"$baseUrl/${dataSet.token}")
    .withFollowRedirects(true)
    .withRequestTimeout(5.minutes)
    .stream()

  /**
    * Retrieve a data.world csv, parsed as an expected type.
    */
  def getCsv[T](dataSet: DataWorldDataSet)(implicit extract: csv.Extractor[T]): Source[T, _] = Source
    .fromFutureSource(getDataSet(dataSet).map(_.bodyAsSource))
    .via(CsvParsing.lineScanner(escapeChar = CsvParsing.DoubleQuote))
    .via(CsvToMap.toMap())
    .map(_.mapValues(_.utf8String))
    .via(Flow.fromFunction(extract))
    .recover {
      case ex =>
        logger.error(s"[${dataSet.displayName}] Error: unable to fully parse CSV. Data may be missing.", ex)
        Failure(ex)
    }
    .collect { case Success(t) => t }
    .via(Flow.recordCountingFlow(dataSet.displayName))
}
