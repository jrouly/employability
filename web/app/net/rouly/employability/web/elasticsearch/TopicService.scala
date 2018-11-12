package net.rouly.employability.web.elasticsearch

import akka.NotUsed
import akka.stream.scaladsl.Source
import net.rouly.employability.elasticsearch.ElasticsearchModule
import net.rouly.employability.models.Topic

class TopicService(elasticsearch: ElasticsearchModule) {

  /**
    * Read all topics from Elasticsearch and return a future with them.
    */
  def topicSource: Source[Topic, NotUsed] = {
    elasticsearch.streams
      .source(elasticsearch.config.topicIndex)
      .map { searchHit =>
        val source = searchHit.sourceAsMap
        Topic(
          id = source("id").toString,
          wordFrequency = source("wordFrequency").asInstanceOf[Map[String, Double]]
        )
      }
  }

}
