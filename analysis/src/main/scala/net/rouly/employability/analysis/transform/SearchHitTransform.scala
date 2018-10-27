package net.rouly.employability.analysis.transform

import akka.NotUsed
import akka.stream.scaladsl.Flow
import com.sksamuel.elastic4s.http.search.SearchHit
import net.rouly.employability.models.JobPosting

object SearchHitTransform {

  object JobPosting {

    def flow: Flow[SearchHit, JobPosting, NotUsed] = Flow.fromFunction(transform)

    private def transform(searchHit: SearchHit): JobPosting = {
      val map = searchHit.sourceAsMap.mapValues(_.toString)

      ???
    }

  }
}
