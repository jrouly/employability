package net.rouly.employability.ingest.elasticsearch

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl.Sink
import com.sksamuel.elastic4s.http._
import com.sksamuel.elastic4s.streams.ReactiveElastic._
import com.sksamuel.elastic4s.streams.RequestBuilder
import com.softwaremill.macwire.Module
import net.rouly.common.config.Configuration

@Module
class ElasticsearchModule(configuration: Configuration)(implicit actorSystem: ActorSystem) {

  // Expected Elasticsearch URL format: "http(s)://host:port,host:port(/prefix)?querystring"
  private lazy val baseUrl: String = configuration.get("elasticsearch.url", "http://localhost:9200")
  private lazy val properties: ElasticProperties = ElasticProperties(baseUrl)
  private lazy val client: ElasticClient = ElasticClient(properties)

  def close(): Unit = client.close()

  def sink[T: RequestBuilder]: Sink[T, NotUsed] = Sink.fromSubscriber(client.subscriber[T]())

}
