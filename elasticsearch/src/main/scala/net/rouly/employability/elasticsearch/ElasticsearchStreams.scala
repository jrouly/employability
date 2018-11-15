package net.rouly.employability.elasticsearch

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl.{Sink, Source}
import com.sksamuel.elastic4s.http.ElasticClient
import com.sksamuel.elastic4s.http.search.SearchHit
import com.sksamuel.elastic4s.searches.SearchRequest
import com.sksamuel.elastic4s.streams.ReactiveElastic._
import com.sksamuel.elastic4s.streams.RequestBuilder

class ElasticsearchStreams(client: ElasticClient)(implicit actorSystem: ActorSystem) {

  /**
    * Stream out an entire index.
    */
  def source(index: String): Source[SearchHit, NotUsed] = Source.fromPublisher(client.publisher(index))

  /**
    * Stream the result of a search.
    */
  def source(q: SearchRequest): Source[SearchHit, NotUsed] = Source.fromPublisher(client.publisher(q))

  /**
    * Stream the result of a search, limited to a certain number of results.
    *
    * @param elements upper limit on number of results to return
    */
  def source(q: SearchRequest, elements: Long): Source[SearchHit, NotUsed] =
    Source.fromPublisher(client.publisher(q, elements))

  /**
    * Stream in data to an index.
    */
  def sink[T: RequestBuilder]: Sink[T, NotUsed] = Sink.fromSubscriber(client.subscriber[T]())

}
