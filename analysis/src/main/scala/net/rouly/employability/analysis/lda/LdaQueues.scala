package net.rouly.employability.analysis.lda

import akka.stream.scaladsl.{Keep, Source, SourceQueueWithComplete}
import akka.stream.{Materializer, OverflowStrategy}
import net.rouly.common.config.Configuration
import net.rouly.employability.elasticsearch.ElasticsearchModule
import net.rouly.employability.models.{ModeledDocument, Topic}

class LdaQueues(
  configuration: Configuration,
  elasticsearch: ElasticsearchModule
)(implicit materializer: Materializer) {

  private val queueBandwidth = configuration.get("queue.bandwidth", "100").toInt

  import elasticsearch.mapping._

  lazy val topics: SourceQueueWithComplete[Topic] = {
    Source.queue[Topic](queueBandwidth, OverflowStrategy.backpressure)
      .watchTermination()(Keep.left)
      .to(elasticsearch.streams.sink[Topic])
      .run()
  }

  lazy val documents: SourceQueueWithComplete[ModeledDocument] = {
    Source.queue[ModeledDocument](queueBandwidth, OverflowStrategy.backpressure)
      .watchTermination()(Keep.left)
      .to(elasticsearch.streams.sink[ModeledDocument])
      .run()
  }

}

/**
  * This global singleton object is required by Spark as each emission task must be "serializeable".
  */
object LdaQueues {
  private var queues: LdaQueues = _

  def setQueues(queues: LdaQueues): Unit = this.queues = queues

  def emit(topic: Topic) = queues.topics.offer(topic)
  def emit(document: ModeledDocument) = queues.documents.offer(document)
}
