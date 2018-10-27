package net.rouly.employability.analysis.transform

import java.util.UUID

import akka.NotUsed
import akka.stream.scaladsl.Flow
import com.sksamuel.elastic4s.http.search.SearchHit
import net.rouly.employability.analysis.models.Document

/**
  * Transform structured ES [[SearchHit]] results into a [[Document]] model.
  */
object DocumentTransformFlow {

  def apply(): Flow[SearchHit, Document[String], NotUsed] = Flow[SearchHit]
    .map {
      case JobPostingSearchHit(jobPosting) => Some(jobPosting)
      case _ => None
    }
    .collect { case Some(doc) => doc }

  private object JobPostingSearchHit extends SearchHitExtractor("job", "id", "description")

  private abstract class SearchHitExtractor(
    `type`: String,
    idField: String,
    contentField: String
  ) {

    def unapply(hit: SearchHit): Option[Document[String]] = {
      if (hit.`type` != `type`) None
      else {
        val source = hit.sourceAsMap
        for {
          id <- source.get(idField)
          content <- source.get(contentField)
        } yield Document(UUID.fromString(id.toString), content.toString)
      }
    }
  }

}
