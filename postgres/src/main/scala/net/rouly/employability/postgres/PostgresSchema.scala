package net.rouly.employability.postgres

import akka.stream.alpakka.slick.scaladsl.SlickSession

class PostgresSchema(implicit val session: SlickSession) {
  import session.profile.api._

  class Documents(tag: Tag) extends Table[(String, String, String, String, String)](tag, "documents") {
    def id = column[String]("id", O.PrimaryKey)
    def raw = column[String]("raw")
    def content = column[String]("content")
    def kind = column[String]("kind")
    def dataSet = column[String]("dataset")
    def * = (id, raw, content, kind, dataSet)
  }

  val documents = TableQuery[Documents]

}
