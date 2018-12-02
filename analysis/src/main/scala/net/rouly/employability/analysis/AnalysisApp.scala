package net.rouly.employability.analysis

import com.softwaremill.macwire.wire
import com.typesafe.scalalogging.StrictLogging
import net.rouly.employability.EmployabilityApp
import net.rouly.employability.analysis.lda.LdaModule
import net.rouly.employability.elasticsearch.ElasticsearchModule
import net.rouly.employability.postgres.PostgresModule
import org.apache.spark.sql.SparkSession

import scala.concurrent.duration._

object AnalysisApp
  extends App
  with EmployabilityApp
  with StrictLogging {

  lazy val elasticsearch: ElasticsearchModule = wire[ElasticsearchModule]
  lazy val postgres: PostgresModule = wire[PostgresModule]
  lazy val lda: LdaModule = wire[LdaModule]

  lazy val spark = SparkSession
    .builder()
    .master("local[12]")
    .getOrCreate()

  // Register shutdown hooks.
  actorSystem.registerOnTermination {
    elasticsearch.close()
    spark.close()
  }

  // Build application future.
  val future = for {
    _ <- lda.initElasticsearch()
    _ <- lda.execute()
  } yield ()

  // Execute the application.
  run(future, 40.minutes)

}
