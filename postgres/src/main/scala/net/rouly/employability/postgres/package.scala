package net.rouly.employability

import slick.dbio.DBIO

package object postgres {

  type RecordInsertion[T] = T => DBIO[Int]

}
