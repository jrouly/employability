package net.rouly.employability.analysis

import akka.stream.scaladsl._
import com.softwaremill.macwire.wire
import com.typesafe.scalalogging.StrictLogging
import net.rouly.employability.EmployabilityApp
import net.rouly.employability.analysis.transform.SearchHitTransform
import net.rouly.employability.elasticsearch.ElasticsearchModule
import net.rouly.employability.models.JobPosting
import net.rouly.employability.postgres._
import net.rouly.employability.streams._
import org.apache.spark.sql.SparkSession

object AnalysisApp
  extends App
  with EmployabilityApp
  with StrictLogging {

  lazy val elasticsearch: ElasticsearchModule = wire[ElasticsearchModule]
  lazy val postgres: PostgresModule = wire[PostgresModule]

  lazy val spark = SparkSession
    .builder()
    .config("spark.master", "local")
    .getOrCreate()

  actorSystem.registerOnTermination(() => spark.close())

  val graph = {
    import postgres.mapping._

    elasticsearch.streams.source
      .via(SearchHitTransform.JobPosting.flow)
      .alsoTo(postgres.streams.sink[JobPosting])
      .via(Flow.recordCountingFlow("postgres", n = 1000))
      .runWith(Sink.ignore)
  }

  run(graph) {
    spark.close()
    postgres.close()
  }

}
