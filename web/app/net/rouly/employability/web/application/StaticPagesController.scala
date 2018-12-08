package net.rouly.employability.web.application

import akka.stream.Materializer
import play.api.mvc.{AbstractController, ControllerComponents}
import views.html.application

import scala.concurrent.ExecutionContext

class StaticPagesController(
  cc: ControllerComponents
)(implicit mat: Materializer, ec: ExecutionContext) extends AbstractController(cc) {

  def index = {
    Action(Ok(application.index()))
  }

  def about = {
    Action(Ok(application.about()))
  }

}
