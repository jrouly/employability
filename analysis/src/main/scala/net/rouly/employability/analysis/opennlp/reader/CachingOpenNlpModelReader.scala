package net.rouly.employability.analysis.opennlp.reader

import java.io.{File, FileInputStream}
import java.nio.file.Files

import com.typesafe.scalalogging.StrictLogging
import net.rouly.common.config.Configuration
import net.rouly.employability.analysis.opennlp.OpenNlpModel
import net.rouly.employability.analysis.opennlp.reader.CachingOpenNlpModelReader.Cache

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Success, Try}

/**
  * Cache the downloaded model files to disk once retrieved.
  */
class CachingOpenNlpModelReader(
  configuration: Configuration,
  underlying: OpenNlpModelReader
)(implicit ec: ExecutionContext)
  extends OpenNlpModelReader
  with StrictLogging {

  private val cachePath = configuration.get("opennlp.model.cache", "data/opennlp/models")
  private val cache = new Cache(cachePath)

  override def getModel(name: String): Future[OpenNlpModel] = {
    cache.get(name) match {
      // If the cached stream is present, return it.
      case Some(stream) =>
        logger.info(s"Cache hit: [$name]")
        Future.successful(stream)

      // Otherwise, read from the underlying reader and cache the result on completion.
      case None =>
        logger.info(s"Cache miss: [$name]")
        underlying
          .getModel(name)
          .andThen { case Success(stream) => cache.put(stream) }
    }
  }

}

object CachingOpenNlpModelReader {

  private class Cache(path: String) {
    val file = new File(path)
    if (!file.exists) file.mkdirs()
    if (!file.isDirectory) throw new Exception(s"Path [$path] in analysis is not a directory.")

    def get(name: String): Option[OpenNlpModel] = {
      val expectedFileName = s"$path/$name"
      Try(new FileInputStream(new File(expectedFileName)))
        .map(stream => new OpenNlpModel(name, stream))
        .toOption
    }

    def put(model: OpenNlpModel): Unit = {
      Try {
        val expectedPath = new File(s"$path/${model.name}").toPath
        Files.copy(model.stream, expectedPath)
      }
    }
  }

}
