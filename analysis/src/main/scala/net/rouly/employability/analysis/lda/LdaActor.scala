package net.rouly.employability.analysis.lda

import akka.Done
import akka.actor.{Actor, Props}
import com.typesafe.scalalogging.StrictLogging
import net.rouly.employability.analysis.models.Document
import org.apache.spark.streaming.akka.ActorReceiver

class LdaActor()
  extends ActorReceiver
  with Actor
  with StrictLogging {

  override def receive: Receive = {

    case Document(id, content: String) =>
      logger.trace(s"Received doc [$id].")
      store(content)

    case Done =>
      logger.info(s"Done.")

  }

}

object LdaActor {

  def props(): Props = Props(new LdaActor())

}
