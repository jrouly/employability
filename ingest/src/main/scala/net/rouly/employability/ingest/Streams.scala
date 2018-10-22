package net.rouly.employability.ingest

import java.util.concurrent.atomic.AtomicInteger

import akka.NotUsed
import akka.stream.scaladsl._
import com.typesafe.scalalogging.StrictLogging

object Streams extends StrictLogging {

  def recordCountingFlow[T](id: String): Flow[T, T, NotUsed] = {
    val counter = new AtomicInteger()
    Flow.fromFunction { t =>
      if (counter.intValue() % 100 == 0) logger.info(s"[$id] Processed ${counter.getAndIncrement()} units.")
      else counter.incrementAndGet()
      t
    }
  }

}
