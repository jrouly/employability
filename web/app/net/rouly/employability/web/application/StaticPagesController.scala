package net.rouly.employability.web.application

import akka.stream.Materializer
import play.api.cache.Cached
import play.api.mvc.{AbstractController, ControllerComponents}
import views.html.application

import scala.concurrent.ExecutionContext

class StaticPagesController(
  cc: ControllerComponents,
  cached: Cached
)(implicit mat: Materializer, ec: ExecutionContext) extends AbstractController(cc) {

  def index = cached("app.index") {
    Action(Ok(application.index()))
  }

  def about = cached("app.about") {
    Action(Ok(application.about()))
  }

}
