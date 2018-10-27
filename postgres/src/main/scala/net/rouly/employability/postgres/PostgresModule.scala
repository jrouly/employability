package net.rouly.employability.postgres

import akka.actor.ActorSystem
import akka.stream.alpakka.slick.scaladsl.SlickSession
import com.softwaremill.macwire.{Module, wire}
import net.rouly.common.config.Configuration
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

@Module
class PostgresModule(
  configuration: Configuration
)(implicit actorSystem: ActorSystem, ec: ExecutionContext) {

  private val databaseConfig = DatabaseConfig.forConfig[JdbcProfile]("employability-postgres")

  implicit val session: SlickSession = SlickSession.forConfig(databaseConfig)
  import session.profile.api._

  lazy val schema: PostgresSchema = wire[PostgresSchema]
  lazy val mapping: PostgresMapping = wire[PostgresMapping]
  lazy val streams: PostgresStreams = wire[PostgresStreams]

  def init(): Future[Unit] = {
    val create = DBIO.seq(
      schema.documents.schema.truncate,
      schema.documents.schema.create
    )
    session.db
      .run(create)
      .recover { case NonFatal(_) => () }
  }

  def close(): Unit = {
    session.close()
  }

}
