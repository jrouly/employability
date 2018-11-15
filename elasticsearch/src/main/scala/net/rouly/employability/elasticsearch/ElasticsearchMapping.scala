package net.rouly.employability.elasticsearch

import com.sksamuel.elastic4s.bulk.BulkCompatibleRequest
import com.sksamuel.elastic4s.http.ElasticDsl._
import com.sksamuel.elastic4s.playjson._
import com.sksamuel.elastic4s.streams.RequestBuilder
import net.rouly.employability.models.{JobPosting, ModeledDocument, Topic}

class ElasticsearchMapping(config: ElasticsearchConfig) {

  implicit val jobPostingRequestBuilder: RequestBuilder[JobPosting] = new RequestBuilder[JobPosting] {
    override def request(t: JobPosting): BulkCompatibleRequest = indexInto(config.jobPostingIndex / "doc")
      .id(t.id.toString)
      .source(t)
  }

  implicit val topicRequestBuilder: RequestBuilder[Topic] = new RequestBuilder[Topic] {
    override def request(t: Topic): BulkCompatibleRequest = indexInto(config.topicIndex / "doc")
      .id(t.id.toString)
      .source(t)
  }

  implicit val modeledDocumentRequestBuilder: RequestBuilder[ModeledDocument] = new RequestBuilder[ModeledDocument] {
    override def request(t: ModeledDocument): BulkCompatibleRequest = indexInto(config.modeledDocumentIndex / "doc")
      .id(t.id.toString)
      .source(t)
  }

}
