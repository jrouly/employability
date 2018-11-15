package net.rouly.employability.preprocess.opennlp.reader

import net.rouly.employability.preprocess.opennlp.OpenNlpModel

import scala.concurrent.Future

trait OpenNlpModelReader {

  def getModel(name: String, baseUrl: String): Future[OpenNlpModel]

}
