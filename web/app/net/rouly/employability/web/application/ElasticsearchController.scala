package net.rouly.employability.web.application

import akka.stream.Materializer
import akka.stream.scaladsl.Sink
import net.rouly.employability.web.application.model.BucketStats
import net.rouly.employability.web.elasticsearch.ElasticsearchWebService
import play.api.cache.Cached
import play.api.mvc.{AbstractController, ControllerComponents}
import views.html.application

import scala.concurrent.ExecutionContext

class ElasticsearchController(
  cc: ControllerComponents,
  cached: Cached,
  service: ElasticsearchWebService
)(implicit mat: Materializer, ec: ExecutionContext) extends AbstractController(cc) {

  def data = cached("app.data") {
    Action.async {
      for {
        rawKindStats <- service.bucket("kind.keyword", rawDocuments = true)
        kindStats <- service.bucket("kind")
        courseDescriptionStats <- service.bucket("dataSet", ("kind", "course-description"))
        jobDescriptionStats <- service.bucket("dataSet", ("kind", "job-description"))
        documentCount <- service.documentCount
        topicCount <- service.topicCount
      } yield Ok(application.data(
        rawDataSetStats = BucketStats("total data set (raw)", ("kind", "count"), rawKindStats.toList),
        modeledDataSetStats = BucketStats("total data set (modeled)", ("kind", "count"), kindStats.toList),
        courseDescriptionStats = BucketStats("course descriptions (modeled)", ("source", "count"), courseDescriptionStats.toList),
        jobDescriptionStats = BucketStats("job descriptions (modeled)", ("source", "count"), jobDescriptionStats.toList),
        documentCount.result.count,
        topicCount.result.count
      ))
    }
  }

  def allTopics = cached("app.allTopics") {
    Action.async {
      for {
        topics <- service.topicSource.runWith(Sink.collection)
      } yield Ok(application.topics(topics.toList.sortBy(_.id.toInt)))
    }
  }

  def topicById(id: String) = cached(s"app.topicById.$id") {
    Action.async {
      for {
        topic <- service.topicSource.filter(_.id == id).runWith(Sink.headOption)
        docs <- service.documentsByTopic(id).take(10).runWith(Sink.collection)
      } yield {
        topic.render(application.topic(_, docs.toList))
      }
    }
  }

  def allDocuments = cached("app.allDocuments") {
    Action.async {
      for {
        docs <- service.documentSource.take(5).runWith(Sink.collection)
        count <- service.documentCount
      } yield Ok(application.documents(docs.toList, count.result.count))
    }
  }

  def docById(id: String) = cached(s"app.docById.$id") {
    Action.async {
      for {
        doc <- service.documentSource.filter(_.id == id).runWith(Sink.headOption)
      } yield doc.render(application.document.apply)
    }
  }

}
