package net.rouly.employability.analysis.lda

import akka.actor.ActorSystem
import akka.stream.Materializer
import com.sksamuel.elastic4s.http._
import com.sksamuel.elastic4s.mappings.FieldDefinition
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

  def initElasticsearch(): Future[Unit] = {
    import com.sksamuel.elastic4s.http.ElasticDsl._

    // This is nested under the ModeledDocument as well as Topic.
    val topicSchema: Seq[FieldDefinition] = List(
      keywordField("id").index(true),
      nestedField("wordFrequency").fields(
        textField("word"),
        doubleField("frequency")
      )
    )

    val modeledDocumentFuture = elasticsearch.client.execute {
      createIndex(elasticsearch.config.modeledDocumentIndex).mappings(ElasticDsl.mapping("doc").fields(
        textField("id"),
        keywordField("kind").index(true),
        keywordField("dataSet").index(true),
        textField("originalText"),
        textField("tokens"),
        nestedField("weightedTopics").fields(
          objectField("topic").fields(topicSchema),
          doubleField("weight")
        )
      ))
    }

    val topicFuture = elasticsearch.client.execute {
      createIndex(elasticsearch.config.topicIndex)
        .mappings(ElasticDsl.mapping("doc").fields(topicSchema))
    }

    for {
      _ <- modeledDocumentFuture
      _ <- topicFuture
    } yield ()

  }

}
