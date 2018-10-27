package net.rouly.employability.analysis.lda

import akka.actor.ActorSystem
import com.softwaremill.macwire.{Module, wire}
import net.rouly.common.config.Configuration
import org.apache.spark.sql.SparkSession

@Module
class LdaModule(
  configuration: Configuration,
  spark: SparkSession
)(implicit actorSystem: ActorSystem) {

  private lazy val ldaConfig: LdaConfig = wire[LdaConfig]

  lazy val processor: LdaProcessor = wire[LdaProcessor]

}
