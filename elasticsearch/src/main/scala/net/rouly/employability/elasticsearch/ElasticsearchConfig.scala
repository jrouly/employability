package net.rouly.employability.elasticsearch

import net.rouly.common.config.Configuration

class ElasticsearchConfig(configuration: Configuration) {

  // Expected Elasticsearch URL format: "http(s)://host:port,host:port(/prefix)?querystring"
  val baseUrl: String = configuration.get("elasticsearch.url", "http://localhost:9200")

  // Index names.
  val rawDocumentIndex: String = configuration.get("elasticsearch.document.raw.index", "raw-documents")
  val topicIndex: String = configuration.get("elasticsearch.topic.index", "topics")
  val modeledDocumentIndex: String = configuration.get("elasticsearch.document.modeled.index", "modeled-documents")

}
