package net.rouly.employability

import akka.actor.ActorSystem
import akka.stream.Supervision.Decider
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Supervision}
import com.typesafe.scalalogging.StrictLogging
import net.rouly.common.config.Configuration

import scala.concurrent.ExecutionContext

trait EmployabilityApp {

  self: StrictLogging =>

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

  val configuration: Configuration = Configuration.default

}
