package net.rouly.employability.web

import play.api.mvc.{Result, Results}
import play.twirl.api.Html

import scala.concurrent.{ExecutionContext, Future}

package object application {

  implicit class RenderOption[T](option: Option[T]) {
    def as(r: T => Html): Result = option match {
      case Some(t) => Results.Ok(r(t))
      case None => Results.NotFound
    }
  }

  implicit class RenderT[T](t: T) {
    def as(r: T => Html): Result = Results.Ok(r(t))
  }

  implicit class RenderFutureT[T](ft: Future[T]) {
    def as(r: T => Html)(implicit ec: ExecutionContext): Future[Result] = ft.map(t => Results.Ok(r(t)))
  }

  implicit class RichResult(result: Result) {
    def cached: Result = result.withHeaders("Cache-Control" -> "public")
    def notCached: Result = result.withHeaders("Cache-Control" -> "no-store")
  }

  implicit class RichFutureResult(fresult: Future[Result]) {
    def cached(implicit ec: ExecutionContext): Future[Result] = fresult.map(_.withHeaders("Cache-Control" -> "public"))
    def notCached(implicit ec: ExecutionContext): Future[Result] = fresult.map(_.withHeaders("Cache-Control" -> "no-store"))
  }

}
