package net.rouly.employability.elasticsearch

import akka.actor.ActorSystem
import com.sksamuel.elastic4s.http._
import com.sksamuel.elastic4s.http.index.CreateIndexResponse
import com.softwaremill.macwire.{Module, wire}
import net.rouly.common.config.Configuration

@Module
class ElasticsearchModule(configuration: Configuration)(implicit actorSystem: ActorSystem) {

  lazy val config: ElasticsearchConfig = wire[ElasticsearchConfig]

  private lazy val properties: ElasticProperties = ElasticProperties(config.baseUrl)

  lazy val client: ElasticClient = ElasticClient(properties)
  lazy val mapping: ElasticsearchMapping = wire[ElasticsearchMapping]
  lazy val streams: ElasticsearchStreams = wire[ElasticsearchStreams]

  def init(): Response[CreateIndexResponse] = {
    import com.sksamuel.elastic4s.http.ElasticDsl._

    client.execute {
      createIndex(config.modeledDocumentIndex).mappings(ElasticDsl.mapping("doc").fields(
        textField("id"),
        textField("originalText"),
        textField("tokens"),
        nestedField("weightedTopics").fields(
          objectField("topic").fields(
            textField("id"),
            objectField("wordFrequency")
          ),
          doubleField("weight")
        )
      ))
    }.await

    client.execute {
      createIndex(config.topicIndex).mappings(ElasticDsl.mapping("doc").fields(
        textField("id"),
        objectField("wordFrequency")
      ))
    }.await
  }

  def close(): Unit = {
    client.close()
  }

}
