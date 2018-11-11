package net.rouly.employability.elasticsearch

import com.sksamuel.elastic4s.bulk.BulkCompatibleRequest
import com.sksamuel.elastic4s.http.ElasticDsl._
import com.sksamuel.elastic4s.playjson._
import com.sksamuel.elastic4s.streams.RequestBuilder
import net.rouly.employability.models.JobPosting
import play.api.libs.json.{Format, Json}

class ElasticsearchMapping(config: ElasticsearchConfig) {

  implicit val jobPostingFormat: Format[JobPosting] = Json.format[JobPosting]

  implicit val jobPostingRequestBuilder: RequestBuilder[JobPosting] = new RequestBuilder[JobPosting] {
    override def request(t: JobPosting): BulkCompatibleRequest = indexInto(config.jobPostingIndex / "doc")
      .id(t.id.toString)
      .source(t)
  }

}
