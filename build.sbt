import Bintray._
import Dependencies._

name := "employability"

lazy val commonSettings = Seq(
  resolvers += Resolver.bintrayRepo("jrouly", "sbt-release"),
  licenses += ("MIT", url("http://opensource.org/licenses/MIT")),
  organization := "net.rouly",
  scalaVersion := "2.11.12",
  name := s"employability-${name.value}"
) ++ bintraySettings

lazy val root = (project in file("."))
  .aggregate(core, elasticsearch, postgres, ingest, analysis, web)

lazy val core = project
  .disablePlugins(BackgroundRunPlugin)
  .settings(commonSettings)
  .settings(libraryDependencies ++= Seq(
    libCommon,
    Akka.streams,
    Logging.logback,
    Logging.scalaLogging,
    Macwire.macros,
    Macwire.util
  ))

lazy val elasticsearch = project
  .dependsOn(core)
  .settings(commonSettings)
  .settings(libraryDependencies ++= Seq(
    Elasticsearch.elastic4sCore,
    Elasticsearch.elastic4sHttp,
    Elasticsearch.elastic4sStreams,
    Elasticsearch.elastic4sPlayJson
  ))

lazy val postgres = project
  .dependsOn(core)
  .settings(commonSettings)
  .settings(libraryDependencies ++= Seq(
    Alpakka.slick,
    Postgres.libCommonDatabase,
    Postgres.postgres,
    Postgres.slick,
    Postgres.slickPostgres
  ))

lazy val ingest = project
  .dependsOn(elasticsearch)
  .settings(commonSettings)
  .settings(libraryDependencies ++= Seq(
    Alpakka.csv,
    Play26.json,
    Play26.ws
  ))

lazy val analysis = project
  .dependsOn(elasticsearch, postgres)
  .settings(commonSettings)
  .settings(libraryDependencies ++= Seq(
    Apache.bahirAkka,
    Apache.openNlp,
    Play26.json,
    Play26.ws,
    Spark.core,
    Spark.mllib
  ))

lazy val web = project
  .enablePlugins(PlayScala, DockerPlugin)
  .dependsOn(postgres)
  .settings(commonSettings)
  .settings(dockerBaseImage := "openjdk:8-jre")
  .settings(dockerRepository := Some("jrouly"))
  .settings(dockerUpdateLatest := true)
  .settings(libraryDependencies ++= Seq(
    Play26.json,
    Play26.test,
    Play26.server,
    Play26.libServer
  ))

fork := true
