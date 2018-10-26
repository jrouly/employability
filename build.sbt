import Bintray._
import Dependencies._

name := "employability"

lazy val root = (project in file("."))
  .aggregate(ingest, analysis, web)

lazy val commonSettings = Seq(
  licenses += ("MIT", url("http://opensource.org/licenses/MIT")),
  organization := "net.rouly",
  scalaVersion := "2.11.12",
  name := s"employability-${name.value}"
) ++ bintraySettings

lazy val ingest = project
  .settings(commonSettings)
  .settings(libraryDependencies ++= Seq(
    libCommon,
    Akka.streams,
    Alpakka.csv,
    Elasticsearch.elastic4sCore,
    Elasticsearch.elastic4sHttp,
    Elasticsearch.elastic4sStreams,
    Elasticsearch.elastic4sPlayJson,
    Logging.logback,
    Logging.scalaLogging,
    Macwire.macros,
    Macwire.util,
    Play26.json,
    Play26.ws
  ))

lazy val analysis = project
  .settings(commonSettings)
  .settings(libraryDependencies ++= Seq(
    Logging.logback,
    Logging.scalaLogging,
    Spark.core
  ))

lazy val web = project
  .enablePlugins(PlayScala, DockerPlugin)
  .settings(commonSettings)
  .settings(dockerBaseImage := "openjdk:8-jre")
  .settings(dockerRepository := Some("jrouly"))
  .settings(dockerUpdateLatest := true)
  .settings(libraryDependencies ++= Seq(
    Elasticsearch.elastic4sCore,
    Elasticsearch.elastic4sHttp,
    Elasticsearch.elastic4sStreams,
    Elasticsearch.elastic4sPlayJson,
    Logging.logback,
    Logging.scalaLogging,
    Macwire.macros,
    Macwire.util,
    Play26.json,
    Play26.test,
    Play26.server,
    Play26.libServer
  ))

resolvers += Resolver.bintrayRepo("jrouly", "sbt-release")
