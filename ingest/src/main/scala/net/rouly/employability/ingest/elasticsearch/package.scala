package net.rouly.employability.ingest

import com.sksamuel.elastic4s.bulk.BulkCompatibleRequest
import com.sksamuel.elastic4s.http.ElasticDsl._
import com.sksamuel.elastic4s.playjson._
import com.sksamuel.elastic4s.streams.RequestBuilder
import net.rouly.employability.ingest.models.JobPosting

package object elasticsearch {

  implicit val requestBuilder: RequestBuilder[JobPosting] = new RequestBuilder[JobPosting] {
    override def request(t: JobPosting): BulkCompatibleRequest = indexInto("employability" / "job")
      .id(t.id.toString)
      .source(t)
  }

}
