package net.rouly.employability.web.application

import akka.stream.Materializer
import akka.stream.scaladsl.Sink
import net.rouly.employability.web.application.model.{BucketStats, OverlapEntry, OverlapStats}
import net.rouly.employability.web.application.service.ElasticsearchWebService
import net.rouly.employability.web.elasticsearch.{DocumentService, TopicService}
import play.api.cache.Cached
import play.api.mvc.{AbstractController, ControllerComponents}
import vegas.spec.Spec.Axis.Properties
import views.html.application

import scala.concurrent.ExecutionContext

class ElasticsearchController(
  cc: ControllerComponents,
  cached: Cached,
  topicService: TopicService,
  documentService: DocumentService,
  webService: ElasticsearchWebService
)(implicit mat: Materializer, ec: ExecutionContext) extends AbstractController(cc) {

  def data = cached("app.data") {
    Action.async {
      for {
        rawKindStats <- webService.bucket("kind.keyword", rawDocuments = true)
        kindStats <- webService.bucket("kind")
        courseDescriptionStats <- webService.bucket("dataSet", ("kind", "course-description"))
        jobDescriptionStats <- webService.bucket("dataSet", ("kind", "job-description"))
        documentCount <- documentService.documentCount
        topicCount <- topicService.topicCount
      } yield Ok(application.data(
        rawDataSetStats = BucketStats("total data set (raw)", ("kind", "count"), rawKindStats.toList),
        modeledDataSetStats = BucketStats("total data set (modeled)", ("kind", "count"), kindStats.toList),
        courseDescriptionStats = BucketStats("course descriptions (modeled)", ("source", "count"), courseDescriptionStats.toList),
        jobDescriptionStats = BucketStats("job descriptions (modeled)", ("source", "count"), jobDescriptionStats.toList),
        documentCount.result.count,
        topicCount.result.count
      )).cached
    }
  }

  def allTopics = cached("app.allTopics") {
    Action.async {
      for {
        topics <- topicService.topicSource.runWith(Sink.collection)
      } yield Ok(application.topics(topics.toList.sortBy(_.id.toInt))).cached
    }
  }

  def topicById(id: String) = cached(s"app.topicById.$id") {
    Action.async {
      for {
        topic <- topicService.topicSource.filter(_.id == id).runWith(Sink.headOption)
        docs <- documentService.documentsByTopic(id).take(10).runWith(Sink.collection)
      } yield {
        topic.render(application.topic(_, docs.toList)).cached
      }
    }
  }

  def allDocuments = cached("app.allDocuments") {
    Action.async {
      for {
        docs <- documentService.documentSource.take(5).runWith(Sink.collection)
        count <- documentService.documentCount
      } yield Ok(application.documents(docs.toList, count.result.count)).cached
    }
  }

  def docById(id: String) = cached(s"app.docById.$id") {
    Action.async {
      for {
        doc <- documentService.documentSource.filter(_.id == id).runWith(Sink.headOption)
      } yield doc.render(application.document.apply).cached
    }
  }

  def overlap = cached("app.overlap") {
    def vegasHtml(stats: OverlapStats): String = {
      import vegas._
      import vegas.render.HTMLRenderer

      def relevant(entry: OverlapEntry): Boolean = {
        val threshold = 0.01
        entry.jobDescriptionProportion > threshold || entry.courseDescriptionProportion > threshold
      }

      def cap(entry: OverlapEntry): OverlapEntry = {
        val threshold = 0.02
        entry.copy(
          jobDescriptionProportion = Math.min(entry.jobDescriptionProportion, threshold),
          courseDescriptionProportion = Math.min(entry.courseDescriptionProportion, threshold)
        )
      }

      val data = stats.entries
        .filter(relevant)
        .sortBy(_.courseDescriptionProportion)
        .flatMap { entry =>
          List(
            Map("Topic" -> entry.topicId, "Proportion" -> entry.courseDescriptionProportion, "Key" -> "course-description"),
            Map("Topic" -> entry.topicId, "Proportion" -> entry.jobDescriptionProportion, "Key" -> "job-description")
          )
        }

      val plot = Vegas(width = 500d, height=250d)
        .withData(data)
        .mark(Bar)
        .configMark(opacity = 0.5)
        .encodeX("Topic", Nominal, axis = Axis(grid = true))
        .encodeY("Proportion", Quantitative, axis = Axis(grid = true, format = "%"))
        .encodeColor(
          field="Key",
          dataType=Nominal,
          legend=Legend(title = "Corpus")
        )

      HTMLRenderer(plot.toJson).plotHTML()
    }

    Action.async {
      for {
        topics <- topicService.topicSource.runWith(Sink.collection)
        overlap <- webService.overlap
      } yield Ok(application.overlap(overlap, topics.toList, vegasHtml(overlap))).cached
    }
  }

}
