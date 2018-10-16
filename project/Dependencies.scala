import sbt._

object Dependencies {

  private val elastic4sVersion = "6.3.7"
  private val elastic4sCore = "com.sksamuel.elastic4s" %% "elastic4s-core" % elastic4sVersion
  private val elastic4sHttp = "com.sksamuel.elastic4s" %% "elastic4s-http" % elastic4sVersion
  private val elastic4sStreams = "com.sksamuel.elastic4s" %% "elastic4s-http-streams" % elastic4sVersion
  val elasticsearch = Seq(elastic4sCore, elastic4sHttp, elastic4sStreams)

  private val logback = "ch.qos.logback" % "logback-classic" % "1.2.3"
  private val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % "3.9.0"
  val logging = Seq(logback, scalaLogging)

}
