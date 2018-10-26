package net.rouly.employability.ingest

import java.util.concurrent.atomic.AtomicInteger

import akka.NotUsed
import akka.stream.scaladsl._
import com.typesafe.scalalogging.StrictLogging

package object streams extends StrictLogging {

  implicit class FlowExtension(val flow: Flow.type) extends AnyVal {
    def recordCountingFlow[T](id: String): Flow[T, T, NotUsed] = {
      val counter = new AtomicInteger()
      Flow.fromFunction { t =>
        if (counter.intValue() % 100 == 0) logger.info(s"[$id] Processed ${counter.getAndIncrement()} units.")
        else counter.incrementAndGet()
        t
      }
    }
  }

  implicit class SourceExtension(val source: Source.type) extends AnyVal {
    def multi[A](sources: Seq[Source[A, _]]): Source[A, _] = {
      sources.toList match {
        case Nil => Source.empty
        case sole :: Nil => sole
        case first :: second :: rest => Source.combine(first, second, rest: _*)(Concat(_))
      }
    }
  }

}
