package net.rouly.employability

import akka.stream.alpakka.slick.scaladsl.SlickSession
import slick.dbio.DBIO
import slick.jdbc.meta.MTable
import slick.lifted.{Query, TableQuery}
import slick.relational.RelationalProfile

import scala.concurrent.{ExecutionContext, Future}

package object postgres {

  type RecordInsertion[T] = T => DBIO[Int]

  type BatchRecordInsertion[T] = Seq[T] => DBIO[Int]

  implicit class RichTable[T <: RelationalProfile#Table[_], U](
    val q: Query[T, U, Seq] with TableQuery[T]
  )(implicit session: SlickSession, ec: ExecutionContext) {
    import session.profile.api._

    def createIfNotExists: Future[Unit] = {
      session.db
        .run(MTable.getTables)
        .flatMap { extantTables =>
          if (extantTables.exists(_.name.name == q.baseTableRow.tableName)) Future.successful(())
          else session.db.run(q.schema.create)
        }
    }

    def dropIfExists: Future[Unit] = {
      session.db
        .run(MTable.getTables)
        .flatMap { extantTables =>
          if (extantTables.exists(_.name.name == q.baseTableRow.tableName)) session.db.run(q.schema.drop)
          else Future.successful(())
        }
    }
  }

}
