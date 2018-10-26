package net.rouly.employability.elasticsearch

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl.{Sink, Source}
import com.sksamuel.elastic4s.ElasticApi._
import com.sksamuel.elastic4s.http._
import com.sksamuel.elastic4s.http.search.SearchHit
import com.sksamuel.elastic4s.streams.ReactiveElastic._
import com.sksamuel.elastic4s.streams.RequestBuilder

class ElasticsearchStreams(
  client: ElasticClient,
  config: ElasticsearchConfig
)(implicit actorSystem: ActorSystem) {

  /**
    * Stream out an entire index per mapping.
    */
  def source(mapping: String): Source[SearchHit, NotUsed] = Source.fromPublisher(client.publisher(config.index / mapping))

  /**
    * Stream out an entire index.
    */
  def source: Source[SearchHit, NotUsed] = Source.fromPublisher(client.publisher(config.index))

  /**
    * Stream in data to an index.
    */
  def sink[T: RequestBuilder]: Sink[T, NotUsed] = Sink.fromSubscriber(client.subscriber[T]())

}
