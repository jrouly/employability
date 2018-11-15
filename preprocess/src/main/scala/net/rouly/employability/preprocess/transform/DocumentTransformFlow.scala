package net.rouly.employability.preprocess.transform

import akka.NotUsed
import akka.stream.scaladsl.Flow
import com.sksamuel.elastic4s.http.search.SearchHit
import com.sksamuel.elastic4s.playjson._
import net.rouly.employability.models.{Document, JobPosting}

import scala.util.{Success, Try}

/**
  * Transform structured ES [[SearchHit]] results into a [[Document]] model.
  */
object DocumentTransformFlow {

  def apply(): Flow[SearchHit, Document[String], NotUsed] = Flow[SearchHit].map(toDoc).collect(some)

  private def toDoc(hit: SearchHit): Option[Document[String]] = {
    val asJobPosting = hit
      .safeToOpt[JobPosting]
      .collect(success)
      .map(toDoc)

    asJobPosting
  }

  private def toDoc(jobPosting: JobPosting): Document[String] = Document(
    id = jobPosting.id,
    raw = jobPosting.description,
    content = jobPosting.description
  )

  private def success[T]: PartialFunction[Try[T], T] = { case Success(t) => t }
  private def some[T]: PartialFunction[Option[T], T] = { case Some(t) => t }

}
