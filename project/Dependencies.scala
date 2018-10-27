import sbt._

object Dependencies {

  private val roulyNetVersion = "0.0.14"
  lazy val libCommon = "net.rouly" %% "lib-common" % roulyNetVersion

  object Akka {
    private val version = "2.5.17"

    lazy val streams = "com.typesafe.akka" %% "akka-stream" % version
  }

  object Alpakka {
    private val version = "0.20"

    lazy val csv = "com.lightbend.akka" %% "akka-stream-alpakka-csv" % version
    lazy val slick = "com.lightbend.akka" %% "akka-stream-alpakka-slick" % version
  }

  object Elasticsearch {
    private val version = "6.3.7"

    lazy val elastic4sCore = "com.sksamuel.elastic4s" %% "elastic4s-core" % version
    lazy val elastic4sHttp = "com.sksamuel.elastic4s" %% "elastic4s-http" % version
    lazy val elastic4sStreams = "com.sksamuel.elastic4s" %% "elastic4s-http-streams" % version
    lazy val elastic4sPlayJson = "com.sksamuel.elastic4s" %% "elastic4s-play-json" % version
  }

  object Logging {
    lazy val logback = "ch.qos.logback" % "logback-classic" % "1.2.3"
    lazy val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % "3.9.0"
  }

  object Macwire {
    private val version = "2.3.1"

    lazy val macros = "com.softwaremill.macwire" %% "macros" % version
    lazy val util = "com.softwaremill.macwire" %% "util" % version
  }

  object Play26 {
    private val version = "2.6.9"

    lazy val json = "com.typesafe.play" %% "play-json" % version
    lazy val test = "com.typesafe.play" %% "play-test" % version % "test"
    lazy val server = "com.typesafe.play" %% "play-server" % version
    lazy val ws = "com.typesafe.play" %% "play-ahc-ws-standalone" % "1.1.10"
    lazy val libServer = "net.rouly" %% "lib-common-server-play26" % roulyNetVersion
  }

  object Postgres {
    lazy val libCommonDatabase = "net.rouly" %% "lib-common-database" % roulyNetVersion
    lazy val postgres = "org.postgresql" % "postgresql" % "42.2.1"
    lazy val slick = "com.typesafe.slick" %% "slick" % "3.2.1"
    lazy val slickPostgres = "com.github.tminglei" %% "slick-pg" % "0.15.7"
  }

  object Spark {
    private val version = "2.3.2"

    lazy val core = "org.apache.spark" %% "spark-core" % version
    lazy val mllib = "org.apache.spark" %% "spark-mllib" % version
  }

}
