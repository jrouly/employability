package net.rouly.employability.streams

import java.time.Instant
import java.util.concurrent.atomic.{AtomicInteger, AtomicLong}

import com.typesafe.scalalogging.StrictLogging

/**
  * Emit occasional log statements describing progress.
  *
  * @param resource name of resource
  * @param seconds number of seconds to emit logs
  */
class BookKeepingWireTap[T](resource: String, seconds: Int) extends (T => Unit) with StrictLogging {
  private val counter = new AtomicInteger()
  private val prev = new AtomicLong()

  override def apply(t: T): Unit = {
    if (emit)
      logger.info(s"[$resource] Processed ${counter.incrementAndGet()} units.")
    else
      counter.incrementAndGet()
  }

  private def emit: Boolean = {
    val currentSecond = Instant.now.getEpochSecond
    if (currentSecond == prev.get) false
    else {
      prev.set(currentSecond)
      currentSecond % seconds == 0
    }
  }
}

object BookKeepingWireTap {
  def apply[T](name: String, seconds: Int = 1): (T => Unit) = new BookKeepingWireTap[T](name, seconds)
}
