package net.rouly.employability.web

import akka.actor.ActorSystem
import com.softwaremill.macwire.wire
import controllers.AssetsComponents
import net.rouly.common.server.play.module.AppServerComponents
import net.rouly.employability.elasticsearch.ElasticsearchModule
import net.rouly.employability.web.api.ApiController
import net.rouly.employability.web.application.service.ElasticsearchWebService
import net.rouly.employability.web.application.{ElasticsearchController, Parameters, StaticPagesController}
import net.rouly.employability.web.echo.EchoController
import net.rouly.employability.web.elasticsearch.{DocumentService, TopicService}
import play.api.BuiltInComponents
import play.api.routing.Router
import router.Routes

trait AppComponents
  extends AppServerComponents
  with AssetsComponents {

  self: BuiltInComponents =>

  implicit val implicitActorSystem: ActorSystem = actorSystem

  lazy val parameters: Parameters = wire[Parameters]
  lazy val elasticsearch: ElasticsearchModule = wire[ElasticsearchModule]

  lazy val documentService: DocumentService = wire[DocumentService]
  lazy val topicService: TopicService = wire[TopicService]
  lazy val webService: ElasticsearchWebService = wire[ElasticsearchWebService]

  lazy val echoController: EchoController = wire[EchoController]
  lazy val elasticsearchController: ElasticsearchController = wire[ElasticsearchController]
  lazy val staticPagesController: StaticPagesController = wire[StaticPagesController]
  lazy val topicController: ApiController = wire[ApiController]

  override lazy val router: Router = {
    lazy val prefix = "/" // routing prefix
    wire[Routes]
  }
}
