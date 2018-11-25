package net.rouly.employability.blocking

import akka.actor.ActorSystem

import scala.concurrent.ExecutionContext

/**
  * Type specifically dedicated for running blocking operations.
  */
class BlockingExecutionContext(underlying: ExecutionContext) extends ExecutionContext {

  override def execute(runnable: Runnable): Unit = underlying.execute(runnable)

  override def reportFailure(cause: Throwable): Unit = underlying.reportFailure(cause)

}

object BlockingExecutionContext {
  def apply(ec: ExecutionContext): BlockingExecutionContext = new BlockingExecutionContext(ec)
  def byName(name: String)(implicit actorSystem: ActorSystem): BlockingExecutionContext =
    apply(actorSystem.dispatchers.lookup(name))
}
