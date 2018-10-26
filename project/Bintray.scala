import sbt._
import bintray.BintrayKeys._

object Bintray {

  lazy val bintraySettings = Seq(
    bintrayPackageLabels := Seq("scala"),
    bintrayRepository := "sbt-release"
  )

}
