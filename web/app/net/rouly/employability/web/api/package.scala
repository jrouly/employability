package net.rouly.employability.web

import akka.stream.scaladsl.Source
import play.api.libs.json.{JsValue, Writes}
import play.api.mvc.Result
import play.api.mvc.Results.Ok

package object api {

  implicit class JsonChunker[M](source: Source[JsValue, M]) {
    def chunkedResponse: Result = Ok.chunked(source).as("application/json")
  }

  implicit class JsonableStream[T, M](source: Source[T, M])(implicit writes: Writes[T]) {
    def chunkedResponse: Result = source.map(writes.writes).chunkedResponse
  }

}
