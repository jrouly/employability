package net.rouly.employability.web.echo

import play.api.mvc.{AbstractController, ControllerComponents}

class EchoController(cc: ControllerComponents)
  extends AbstractController(cc) {

  def echo = Action(Ok("echo"))

}
