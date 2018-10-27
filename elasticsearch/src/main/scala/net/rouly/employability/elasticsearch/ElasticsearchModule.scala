package net.rouly.employability.elasticsearch

import akka.actor.ActorSystem
import com.sksamuel.elastic4s.http._
import com.softwaremill.macwire.{Module, wire}
import net.rouly.common.config.Configuration

@Module
class ElasticsearchModule(configuration: Configuration)(implicit actorSystem: ActorSystem) {

  private lazy val esConfig: ElasticsearchConfig = wire[ElasticsearchConfig]
  private lazy val properties: ElasticProperties = ElasticProperties(esConfig.baseUrl)
  private lazy val client: ElasticClient = ElasticClient(properties)

  lazy val mapping: ElasticsearchMapping = wire[ElasticsearchMapping]
  lazy val streams: ElasticsearchStreams = wire[ElasticsearchStreams]

  def close(): Unit = {
    client.close()
  }

}
