name := "employability"

lazy val commonSettings = Seq(
  licenses += ("MIT", url("http://opensource.org/licenses/MIT")),
  organization := "net.rouly",
  scalaVersion := "2.12.2",
  name := s"employability-${name.value}"
)

lazy val root = (project in file("."))
  .aggregate(ingest)

lazy val ingest = project
  .settings(commonSettings)
  .settings(libraryDependencies ++= Seq(
    "com.sksamuel.elastic4s" %% "elastic4s-core" % "6.3.7"
  ))
