package net.rouly.employability

import akka.stream.scaladsl._
import com.typesafe.scalalogging.StrictLogging

package object streams extends StrictLogging {

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
