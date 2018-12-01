package net.rouly.employability.web

import akka.actor.ActorSystem
import com.softwaremill.macwire.wire
import controllers.AssetsComponents
import net.rouly.common.server.play.module.AppServerComponents
import net.rouly.employability.elasticsearch.ElasticsearchModule
import net.rouly.employability.web.api.ApiController
import net.rouly.employability.web.application.{ElasticsearchController, StaticPagesController}
import net.rouly.employability.web.echo.EchoController
import net.rouly.employability.web.elasticsearch.ElasticsearchWebService
import play.api.BuiltInComponents
import play.api.cache.Cached
import play.api.cache.ehcache.EhCacheComponents
import play.api.routing.Router
import router.Routes

trait AppComponents
  extends AppServerComponents
  with AssetsComponents
  with EhCacheComponents {

  self: BuiltInComponents =>

  implicit val implicitActorSystem: ActorSystem = actorSystem

  private lazy val cached: Cached = new Cached(defaultCacheApi)

  lazy val elasticsearch: ElasticsearchModule = wire[ElasticsearchModule]
  lazy val elasticsearchService: ElasticsearchWebService = wire[ElasticsearchWebService]

  lazy val echoController: EchoController = wire[EchoController]
  lazy val elasticsearchController: ElasticsearchController = wire[ElasticsearchController]
  lazy val staticPagesController: StaticPagesController = wire[StaticPagesController]
  lazy val topicController: ApiController = wire[ApiController]

  override lazy val router: Router = {
    lazy val prefix = "/" // routing prefix
    wire[Routes]
  }
}
