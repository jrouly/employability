package net.rouly.employability.analysis.opennlp.reader

import net.rouly.employability.analysis.opennlp.OpenNlpModel

import scala.concurrent.Future

trait OpenNlpModelReader {

  def getModel(name: String): Future[OpenNlpModel]

}
