package net.rouly.employability.postgres

import akka.stream.alpakka.slick.scaladsl.SlickSession

class PostgresSchema(implicit val session: SlickSession) {
  import session.profile.api._

  class Documents(tag: Tag) extends Table[(String, String)](tag, "documents") {
    def id = column[String]("id", O.PrimaryKey)
    def content = column[String]("content")
    def * = (id, content)
  }

  val documents = TableQuery[Documents]

}
