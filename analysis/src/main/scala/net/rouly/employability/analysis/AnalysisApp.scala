package net.rouly.employability.analysis

import akka.Done
import akka.stream.scaladsl._
import com.softwaremill.macwire.wire
import com.typesafe.scalalogging.StrictLogging
import net.rouly.employability.EmployabilityApp
import net.rouly.employability.analysis.lda.LdaModule
import net.rouly.employability.analysis.opennlp.{AnalysisOpenNlpModels, OpenNlpModule}
import net.rouly.employability.analysis.transform.{DocumentTransformFlow, PreProcessFlow}
import net.rouly.employability.elasticsearch.ElasticsearchModule
import net.rouly.employability.postgres._
import net.rouly.employability.streams._
import org.apache.spark.sql.SparkSession
import play.api.libs.ws.StandaloneWSClient
import play.api.libs.ws.ahc.StandaloneAhcWSClient

import scala.concurrent.Await
import scala.concurrent.duration._

object AnalysisApp
  extends App
  with EmployabilityApp
  with StrictLogging {

  val wsClient: StandaloneWSClient = StandaloneAhcWSClient()

  lazy val elasticsearch: ElasticsearchModule = wire[ElasticsearchModule]
  lazy val postgres: PostgresModule = wire[PostgresModule]
  lazy val opennlp: OpenNlpModule = wire[OpenNlpModule]
  lazy val lda: LdaModule = wire[LdaModule]

  // Blocking IO: Retrieve online binaries of opennlp models.
  val openNlpModels: AnalysisOpenNlpModels = new AnalysisOpenNlpModels(
    placeNameModel = Await.result(opennlp.reader.getModel("en-ner-location.bin"), 2.minutes),
    tokenizerModel = Await.result(opennlp.reader.getModel("en-token.bin"), 2.minutes)
  )

  lazy val spark = SparkSession
    .builder()
    .master("local")
    .getOrCreate()

  val graph = elasticsearch.streams.source
    .take(500) // TODO: DELETEME
    .wireTap(BookKeepingWireTap("elasticsearch"))
    .via(DocumentTransformFlow())
    .via(PreProcessFlow(openNlpModels))
    // .alsoTo(postgres.streams.sink[TopicModel])
    .alsoTo(Sink.actorRef(lda.actor, Done))
    .runWith(Sink.ignore)

  run(graph) {
    elasticsearch.close()
    wsClient.close()
    spark.close()
    postgres.close()
  }

}
