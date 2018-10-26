package net.rouly.employability.analysis

import akka.stream.scaladsl._
import com.softwaremill.macwire.wire
import com.typesafe.scalalogging.StrictLogging
import net.rouly.employability.EmployabilityApp
import net.rouly.employability.analysis.postgres.PostgresModule
import net.rouly.employability.elasticsearch.ElasticsearchModule
import net.rouly.employability.streams._

import scala.concurrent.Await
import scala.concurrent.duration._

object AnalysisApp
  extends App
  with EmployabilityApp
  with StrictLogging {

  lazy val elasticsearch: ElasticsearchModule = wire[ElasticsearchModule]
  lazy val postgres: PostgresModule = wire[PostgresModule]

  val graph = elasticsearch.streams.source
    .via(Flow.recordCountingFlow("postgres"))
    .alsoTo(postgres.streams.sink)
    .runWith(Sink.ignore)

  logger.info("Start.")

  graph.onComplete { _ =>
    elasticsearch.close()
    materializer.shutdown()
    actorSystem.terminate()
  }

  Await.result(graph, 5.minutes)

  logger.info("Done.")

}
