package net.rouly.employability.postgres

import akka.stream.alpakka.slick.scaladsl.SlickSession
import net.rouly.employability.models.Document

import scala.concurrent.ExecutionContext

class PostgresMapping(schema: PostgresSchema)(implicit val session: SlickSession, ec: ExecutionContext) {
  import session.profile.api._

  implicit val documentInsertion: RecordInsertion[Document[String]] = { document =>
    schema.documents += (document.id.toString, document.raw, document.content, document.kind.kind, document.dataSet)
  }

  implicit val batchDocumentInsertion: BatchRecordInsertion[Document[String]] = { documents =>
    (schema.documents ++= documents.map(document => (document.id.toString, document.raw, document.content, document.kind.kind, document.dataSet))).map(_.sum)
  }

}
