package net.rouly.employability.postgres

import akka.stream.alpakka.slick.scaladsl.SlickSession
import net.rouly.employability.models.Document

class PostgresMapping(schema: PostgresSchema)(implicit val session: SlickSession) {
  import session.profile.api._

  implicit val documentInsertion: RecordInsertion[Document[String]] = { document =>
    schema.documents += (document.id.toString, document.content)
  }

}
