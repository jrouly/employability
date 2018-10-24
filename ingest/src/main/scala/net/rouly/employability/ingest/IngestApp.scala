package net.rouly.employability.ingest

import akka.actor.ActorSystem
import akka.stream.Supervision.Decider
import akka.stream.scaladsl._
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Supervision}
import com.softwaremill.macwire.wire
import com.typesafe.scalalogging.StrictLogging
import net.rouly.common.config.Configuration
import net.rouly.employability.ingest.dataworld.DataWorldModule
import net.rouly.employability.ingest.elasticsearch._
import net.rouly.employability.ingest.models.JobPosting
import play.api.libs.ws.StandaloneWSClient
import play.api.libs.ws.ahc.StandaloneAhcWSClient

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext}

object IngestApp extends App with StrictLogging {

  implicit val actorSystem: ActorSystem = ActorSystem()
  implicit val executionContext: ExecutionContext = actorSystem.dispatcher

  private val loggingResumingDecider: Decider = { e =>
    logger.warn(s"Error encountered. Continuing.", e)
    Supervision.Resume
  }

  private val settings: ActorMaterializerSettings =
    ActorMaterializerSettings(actorSystem)
      .withSupervisionStrategy(loggingResumingDecider)

  implicit val materializer: ActorMaterializer = ActorMaterializer(settings)

  val wsClient: StandaloneWSClient = StandaloneAhcWSClient()
  val configuration: Configuration = Configuration.default

  lazy val elasticsearch: ElasticsearchModule = wire[ElasticsearchModule]
  lazy val dataWorld: DataWorldModule = wire[DataWorldModule]

  val graph = dataWorld.source
    .via(Streams.recordCountingFlow("elasticsearch"))
    .alsoTo(elasticsearch.sink[JobPosting])
    .runWith(Sink.ignore)

  logger.info("Start.")

  graph.onComplete { _ =>
    wsClient.close()
    elasticsearch.close()
    materializer.shutdown()
    actorSystem.terminate()
  }

  Await.result(graph, 5.minutes)

  logger.info("Done.")

}
