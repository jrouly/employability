package net.rouly.employability.analysis.lda

import akka.actor.ActorSystem
import akka.stream.Materializer
import com.softwaremill.macwire.{Module, wire}
import net.rouly.common.config.Configuration
import net.rouly.employability.elasticsearch.ElasticsearchModule
import org.apache.spark.sql.SparkSession

import scala.concurrent.{ExecutionContext, Future}

@Module
class LdaModule(
  configuration: Configuration,
  elasticsearch: ElasticsearchModule,
  spark: SparkSession
)(implicit actorSystem: ActorSystem, materializer: Materializer, ec: ExecutionContext) {

  private lazy val config: LdaConfig = wire[LdaConfig]
  private lazy val queues: LdaQueues = wire[LdaQueues]
  private lazy val processor: LdaProcessor = wire[LdaProcessor]

  LdaQueues.setQueues(queues)

  def execute(): Future[Unit] = {

    // Perform LDA and emit topics to queues.
    processor.execute()

    // Close queues.
    queues.topics.complete()
    queues.documents.complete()

    // Wait for completion.
    for {
      _ <- queues.topics.watchCompletion()
      _ <- queues.documents.watchCompletion()
    } yield ()

  }

}
