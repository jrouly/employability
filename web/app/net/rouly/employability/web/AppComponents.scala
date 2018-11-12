package net.rouly.employability.web

import akka.actor.ActorSystem
import com.softwaremill.macwire.wire
import controllers.AssetsComponents
import net.rouly.common.server.play.module.AppServerComponents
import net.rouly.employability.elasticsearch.ElasticsearchModule
import net.rouly.employability.web.api.TopicController
import net.rouly.employability.web.application.ApplicationController
import net.rouly.employability.web.echo.EchoController
import net.rouly.employability.web.elasticsearch.TopicService
import play.api.BuiltInComponents
import play.api.routing.Router
import router.Routes

trait AppComponents
  extends AppServerComponents
    with AssetsComponents {

  self: BuiltInComponents =>

  implicit val implicitActorSystem: ActorSystem = actorSystem

  lazy val echoController: EchoController = wire[EchoController]
  lazy val elasticsearch: ElasticsearchModule = wire[ElasticsearchModule]

  lazy val topicService: TopicService = wire[TopicService]
  lazy val topicController: TopicController = wire[TopicController]

  lazy val applicationController: ApplicationController = wire[ApplicationController]

  override lazy val router: Router = {
    lazy val prefix = "/" // routing prefix
    wire[Routes]
  }
}
