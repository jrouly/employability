package net.rouly.employability.ingest.dataworld.model

case class DataWorldDataSet(
  name: String,
  organization: String,
  dictionary: Map[String, String],
  token: String
) {

  val displayName: String = s"$organization/$name"

}
