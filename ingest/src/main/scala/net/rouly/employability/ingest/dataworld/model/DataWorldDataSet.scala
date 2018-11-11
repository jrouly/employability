package net.rouly.employability.ingest.dataworld.model

case class DataWorldDataSet(
  name: String,
  organization: String,
  fileName: String,
  dictionary: Map[String, String]
) {

  val displayName: String = s"$organization/$name"

}
