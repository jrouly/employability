package net.rouly.employability.postgres

import akka.Done
import akka.stream.alpakka.slick.scaladsl.{Slick, SlickSession}
import akka.stream.scaladsl.Sink

import scala.concurrent.Future

class PostgresStreams(implicit session: SlickSession) {

  def sink[T](implicit insertion: RecordInsertion[T]): Sink[T, Future[Done]] = Slick.sink(insertion)

}
