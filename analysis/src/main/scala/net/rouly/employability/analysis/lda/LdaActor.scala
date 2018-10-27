package net.rouly.employability.analysis.lda

import akka.Done
import akka.actor.{Actor, Props}
import com.typesafe.scalalogging.StrictLogging
import net.rouly.employability.analysis.models.Document

class LdaActor()
  extends Actor
  with StrictLogging {

  override def receive: Receive = {

    case Document(id, content: Seq[String]) =>
      logger.trace(s"Received doc [$id] with first few words: ${content.take(4)}")

    case Done =>
      logger.info(s"Done.")

  }

}

object LdaActor {

  def props(): Props = Props(new LdaActor())

}
