name := "employability"

lazy val root = (project in file("."))
  .aggregate(ingest, web)

lazy val commonSettings = Seq(
  licenses += ("MIT", url("http://opensource.org/licenses/MIT")),
  logLevel := Level.Warn,
  organization := "net.rouly",
  scalaVersion := "2.12.2",
  name := s"employability-${name.value}"
)

lazy val ingest = project
  .settings(commonSettings)
  .settings(libraryDependencies ++= Seq(
    Dependencies.libCommon,
    Dependencies.Akka.streams,
    Dependencies.Alpakka.csv,
    Dependencies.Elasticsearch.elastic4sCore,
    Dependencies.Elasticsearch.elastic4sHttp,
    Dependencies.Elasticsearch.elastic4sStreams,
    Dependencies.Elasticsearch.elastic4sPlayJson,
    Dependencies.Logging.logback,
    Dependencies.Logging.scalaLogging,
    Dependencies.Macwire.macros,
    Dependencies.Macwire.util,
    Dependencies.Play.json,
    Dependencies.Play.ws
  ))

lazy val web = project
  .enablePlugins(PlayScala, DockerPlugin)
  .settings(commonSettings)
  .settings(dockerBaseImage := "openjdk:8-jre")
  .settings(dockerRepository := Some("jrouly"))
  .settings(dockerUpdateLatest := true)
  .settings(libraryDependencies ++= Seq(
    Dependencies.Elasticsearch.elastic4sCore,
    Dependencies.Elasticsearch.elastic4sHttp,
    Dependencies.Elasticsearch.elastic4sStreams,
    Dependencies.Elasticsearch.elastic4sPlayJson,
    Dependencies.Logging.logback,
    Dependencies.Logging.scalaLogging,
    Dependencies.Macwire.macros,
    Dependencies.Macwire.util,
    Dependencies.Play.json,
    Dependencies.Play.test,
    Dependencies.Play.server,
    Dependencies.Play.libServer
  ))
