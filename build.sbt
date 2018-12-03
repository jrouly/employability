import Dependencies._

name := "employability"

lazy val noPublish = Seq(
  publishArtifact := false,
  publishLocal := {},
  publish := {}
)

lazy val commonSettings = Seq(
  licenses += ("MIT", url("http://opensource.org/licenses/MIT")),
  resolvers += Resolver.bintrayRepo("jrouly", "sbt-release"),
  organization := "net.rouly",
  scalaVersion := "2.11.12",
  version := "0.7",
  name := s"employability-${name.value}"
) ++ Bintray.settings

lazy val root = (project in file("."))
  .settings(commonSettings)
  .settings(noPublish)
  .aggregate(
    core,
    elasticsearch,
    postgres,
    ingest,
    preprocess,
    analysis,
    web
  )

lazy val core = project
  .disablePlugins(BackgroundRunPlugin)
  .settings(commonSettings)
  .settings(libraryDependencies ++= Seq(
    libCommon,
    Akka.streams,
    Logging.logback,
    Logging.scalaLogging,
    Macwire.macros,
    Macwire.util,
    Play26.json
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
    Akka.actors,
    Akka.streams,
    Alpakka.csv,
    JSoup.jsoup,
    Misc.scalaUri,
    Play26.json,
    Play26.ws
  ))

lazy val preprocess = project
  .dependsOn(elasticsearch, postgres)
  .settings(commonSettings)
  .settings(libraryDependencies ++= Seq(
    Apache.openNlp,
    Play26.json,
    Play26.ws
  ))

lazy val analysis = project
  .dependsOn(elasticsearch, postgres)
  .settings(commonSettings)
  .settings(libraryDependencies ++= Seq(
    Spark.core,
    Spark.mllib
  ))

lazy val web = project
  .enablePlugins(PlayScala, DockerPlugin)
  .dependsOn(elasticsearch)
  .settings(commonSettings)
  .settings(dockerBaseImage := "openjdk:8-jre")
  .settings(dockerRepository := Some("jrouly"))
  .settings(dockerUpdateLatest := true)
  .settings(libraryDependencies ++= Seq(
    Play26.ehcache,
    Play26.json,
    Play26.test,
    Play26.server,
    Play26.libServer
  ))

fork := true

resolvers += Resolver.bintrayRepo("jrouly", "sbt-release")
