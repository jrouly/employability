package net.rouly.employability

import akka.actor.ActorSystem
import akka.stream.Supervision.Decider
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Supervision}
import com.typesafe.scalalogging.StrictLogging
import net.rouly.common.config.Configuration

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

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

  def run[T](future: Future[T], durationMinutes: Int = 5)(shutdownHooks: => Unit): T = {
    logger.info("Start.")
    val result = Await.result(future, durationMinutes.minutes)
    logger.info("Done.")

    Await.result(actorSystem.terminate(), 5.minutes)

    materializer.shutdown()
    shutdownHooks

    result
  }

}
