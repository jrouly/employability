package net.rouly.employability

import akka.actor.ActorSystem
import akka.stream.Supervision.Decider
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Materializer, Supervision}
import com.typesafe.scalalogging.StrictLogging
import net.rouly.common.config.Configuration
import net.rouly.employability.EmployabilityApp.EmployabilityMaterializer

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

/**
  * Entry-level app mixin.
  */
trait EmployabilityApp {
  self: App with StrictLogging =>

  implicit val actorSystem: ActorSystem = ActorSystem()
  implicit val executionContext: ExecutionContext = actorSystem.dispatcher
  implicit val materializer: Materializer = EmployabilityMaterializer(actorSystem)

  val configuration: Configuration = Configuration.default

  protected lazy val parallelism: Int = Runtime.getRuntime.availableProcessors()

  def run[T](future: Future[T], duration: FiniteDuration = 5.minutes): Unit = {
    // Execute the application.
    logger.info("Start.")
    Await.ready(future, duration)
      .andThen {
        case Success(_) => logger.info("Finished.")
        case Failure(exception) => logger.error("Failed to complete, exception occurred.", exception)
      }
      .onComplete(_ => actorSystem.terminate())
  }

}

object EmployabilityApp extends StrictLogging {

  private object EmployabilityMaterializer {
    def apply(actorSystem: ActorSystem): Materializer = {

      val loggingResumingDecider: Decider = { e =>
        logger.warn(s"Error encountered. Continuing.", e)
        Supervision.Resume
      }

      val settings: ActorMaterializerSettings = ActorMaterializerSettings(actorSystem)
        .withSupervisionStrategy(loggingResumingDecider)

      ActorMaterializer(settings)(actorSystem)
    }
  }

}
