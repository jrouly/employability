package net.rouly.employability.ingest

import akka.Done
import akka.stream.Materializer
import akka.stream.scaladsl.Sink
import net.rouly.employability.elasticsearch.ElasticsearchModule
import net.rouly.employability.ingest.dataworld.DataWorldModule
import net.rouly.employability.models.JobPosting
import net.rouly.employability.streams.BookKeepingWireTap

import scala.concurrent.Future

class IngestStreams(
  dataWorld: DataWorldModule,
  elasticsearch: ElasticsearchModule
)(implicit materializer: Materializer) {

  import elasticsearch.mapping._

  lazy val ingestGraph: Future[Done] = {
    dataWorld.source
      .alsoTo(elasticsearch.streams.sink[JobPosting])
      .wireTap(BookKeepingWireTap("elasticsearch"))
      .runWith(Sink.ignore)
  }

}
