package net.rouly.employability.elasticsearch

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl.{Sink, Source}
import com.sksamuel.elastic4s.http._
import com.sksamuel.elastic4s.http.search.SearchHit
import com.sksamuel.elastic4s.streams.ReactiveElastic._
import com.sksamuel.elastic4s.streams.RequestBuilder

class ElasticsearchStreams(client: ElasticClient)(implicit actorSystem: ActorSystem) {

  /**
    * Stream out an entire index.
    */
  def source(index: String): Source[SearchHit, NotUsed] = Source.fromPublisher(client.publisher(index))

  /**
    * Stream in data to an index.
    */
  def sink[T: RequestBuilder]: Sink[T, NotUsed] = Sink.fromSubscriber(client.subscriber[T]())

}
