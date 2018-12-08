package net.rouly.employability.web

import play.api.mvc.{Result, Results}
import play.twirl.api.Html

package object application {

  implicit class OptionalRender[T](option: Option[T]) {
    def render(r: T => Html): Result = option match {
      case Some(t) => Results.Ok(r(t))
      case None => Results.NotFound
    }
  }

  implicit class RichResult(result: Result) {
    def cached: Result = result.withHeaders("Cache-Control" -> "public")
    def notCached: Result = result.withHeaders("Cache-Control" -> "no-store")
  }

}
