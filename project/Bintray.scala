import sbt._
import bintray.BintrayKeys._

object Bintray {

  lazy val settings = Seq(
    bintrayPackageLabels := Seq("scala"),
    bintrayRepository := "sbt-release"
  )

}
