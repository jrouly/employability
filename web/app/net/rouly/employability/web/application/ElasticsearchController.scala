package net.rouly.employability.web.application

import akka.stream.Materializer
import akka.stream.scaladsl.Sink
import net.rouly.employability.models.DocumentKind
import net.rouly.employability.web.application.model.{BucketStats, OverlapEntry, OverlapStats}
import net.rouly.employability.web.application.service.ElasticsearchWebService
import net.rouly.employability.web.elasticsearch.{DocumentService, TopicService}
import play.api.mvc.{AbstractController, ControllerComponents}
import views.html.application

import scala.concurrent.ExecutionContext

class ElasticsearchController(
  cc: ControllerComponents,
  parameters: Parameters,
  topicService: TopicService,
  documentService: DocumentService,
  webService: ElasticsearchWebService
)(implicit mat: Materializer, ec: ExecutionContext) extends AbstractController(cc) {

  def data = {
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

  def allTopics = {
    Action.async {
      topicService
        .topicSource
        .runWith(Sink.collection)
        .map(_.toList.sortBy(_.id.toInt))
        .as(application.topics.apply)
        .cached
    }
  }

  def topicById(id: String) = {
    Action.async {
      for {
        topic <- topicService.topicById(id)
        docs <- documentService.documentsByTopic(id).take(10).runWith(Sink.collection)
      } yield Ok(application.topic(topic, docs.toList)).cached
    }
  }

  def allDocuments = {
    Action.async {
      for {
        docs <- documentService.documentSource.take(5).runWith(Sink.collection)
        count <- documentService.documentCount
      } yield Ok(application.documents(docs.toList, count.result.count)).cached
    }
  }

  def docById(id: String) = {
    Action.async {
      documentService
        .documentById(id)
        .as(application.document.apply)
        .cached
    }
  }

  def overlap = {
    def vegasHtml(stats: OverlapStats): String = {
      import vegas._
      import vegas.render.HTMLRenderer

      def relevant(strict: Boolean, theta: Double)(entry: OverlapEntry): Boolean = {
        if (strict) entry.jobDescriptionProportion > theta && entry.courseDescriptionProportion > theta
        else entry.jobDescriptionProportion > theta || entry.courseDescriptionProportion > theta
      }

      val data = stats.entries
        .filter(relevant(strict = parameters.strictOverlap, theta = parameters.theta))
        .sortBy(_.topicId)
        .flatMap { entry =>
          List(
            Map("Topic" -> entry.topicId, "Proportion" -> entry.courseDescriptionProportion, "Key" -> "course-description"),
            Map("Topic" -> entry.topicId, "Proportion" -> entry.jobDescriptionProportion, "Key" -> "job-description")
          )
        }

      val plot = Vegas(width = 500d, height = 250d)
        .withData(data)
        .mark(Bar)
        .configMark(opacity = 0.5)
        .encodeX("Topic", Nominal, axis = Axis(grid = true))
        .encodeY("Proportion", Quantitative, axis = Axis(grid = true, format = "%"))
        .encodeColor(
          field = "Key",
          dataType = Nominal,
          legend = Legend(title = "Corpus")
        )

      HTMLRenderer(plot.toJson).plotHTML()
    }

    Action.async {
      for {
        topics <- topicService.topicSource.runWith(Sink.collection)
        jdCount <- webService.countByKind(DocumentKind.JobDescription)
        cdCount <- webService.countByKind(DocumentKind.CourseDescription)
        overlap <- webService.overlap(jdCount, cdCount, parameters.rho)
      } yield Ok(application.overlap(overlap, topics.toList, vegasHtml(overlap), parameters.theta, parameters.strictOverlap)).cached
    }
  }

}
