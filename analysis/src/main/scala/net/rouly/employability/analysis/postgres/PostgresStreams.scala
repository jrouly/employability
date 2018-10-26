package net.rouly.employability.analysis.postgres

import akka.Done
import akka.stream.scaladsl.Sink

import scala.concurrent.Future

class PostgresStreams {

  // TODO
  def sink[T]: Sink[T, Future[Done]] = Sink.ignore

}
