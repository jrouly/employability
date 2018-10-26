package net.rouly.employability.analysis.postgres

import akka.actor.ActorSystem
import com.softwaremill.macwire.{Module, wire}
import net.rouly.common.config.Configuration
import net.rouly.common.database.{ApplicationDatabase, DatabaseExecutionContext, RemoteDatabase, RemoteDatabaseConfiguration}
import slick.jdbc.PostgresProfile

@Module
class PostgresModule(configuration: Configuration)(implicit actorSystem: ActorSystem) {

  // Always separate blocking JDBC IO from the non-blocking Play thread pool.
  // See: https://www.playframework.com/documentation/2.6.x/ScalaDatabase
  private implicit lazy val databaseExecutionContext: DatabaseExecutionContext =
    DatabaseExecutionContext(actorSystem.dispatchers.lookup("database.dispatcher"))

  private lazy val databaseConfig = RemoteDatabaseConfiguration(
    user = configuration.get("db.employability.username", "postgres"),
    password = configuration.get("db.employability.password", ""),
    url = configuration.get("db.employability.url", "jdbc:postgresql://employability-postgres:5432/employability")
  )

  lazy val database: ApplicationDatabase = new RemoteDatabase(databaseConfig, PostgresProfile)

  lazy val streams: PostgresStreams = wire[PostgresStreams]

}
