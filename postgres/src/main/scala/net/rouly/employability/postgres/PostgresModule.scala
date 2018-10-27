package net.rouly.employability.postgres

import akka.actor.ActorSystem
import akka.stream.alpakka.slick.scaladsl.SlickSession
import com.softwaremill.macwire.{Module, wire}
import net.rouly.common.config.Configuration
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

@Module
class PostgresModule(configuration: Configuration)(implicit actorSystem: ActorSystem) {

  private val databaseConfig = DatabaseConfig.forConfig[JdbcProfile]("employability-postgres")
  private implicit val session: SlickSession = SlickSession.forConfig(databaseConfig)

  lazy val mapping: PostgresMapping = wire[PostgresMapping]
  lazy val streams: PostgresStreams = wire[PostgresStreams]

  def close(): Unit = {
    session.close()
  }

}
