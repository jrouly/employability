import bintray.BintrayKeys._

object Bintray {

  lazy val settings = Seq(
    bintrayPackageLabels := Seq("scala"),
    bintrayRepository := "sbt-release",
    bintrayVcsUrl := Some("git@github.com:jrouly/employability")
  )

}
