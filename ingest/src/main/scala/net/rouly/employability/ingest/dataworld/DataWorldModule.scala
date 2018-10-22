package net.rouly.employability.ingest.dataworld

import akka.stream.scaladsl._
import com.softwaremill.macwire.wire
import net.rouly.common.config.Configuration
import net.rouly.employability.ingest.models.JobPosting
import play.api.libs.ws.StandaloneWSClient

import scala.concurrent.ExecutionContext

class DataWorldModule(
  configuration: Configuration,
  wsClient: StandaloneWSClient
)(implicit ec: ExecutionContext) {

  private lazy val client: DataWorldClient = wire[DataWorldClient]

  // https://data.world/promptcloud/us-jobs-on-dice-com
  lazy val usJobs: Source[JobPosting, _] = {
    implicit val extractor: csv.Extractor[JobPosting] = csv.jobPosting("us-jobs", "jobdescription", "jobtitle", Some("skills"))
    client.getCsv[JobPosting]("7gstuwsabsqxrwni4cxsbcmuv5rzbn")
  }

  // https://data.world/promptcloud/50000-job-board-records-from-reed-uk
  lazy val reedUk: Source[JobPosting, _] = {
    implicit val extractor: csv.Extractor[JobPosting] = csv.jobPosting("reed-uk", "job_description", "job_title", Some("job_requirements"))
    client.getCsv[JobPosting]("l7rmmscfypoofjwulzkykg3hgqn5zg")
  }

  // https://data.world/promptcloud/30000-job-postings-from-seek-australia
  lazy val seekAus: Source[JobPosting, _] = {
    implicit val extractor: csv.Extractor[JobPosting] = csv.jobPosting("seek-aus", "job_description", "job_title")
    client.getCsv[JobPosting]("75iczjrunbdiuxytp2aywuh5hdlgdz")
  }

  def publisher: Source[JobPosting,_] =
    Source.combine(
      usJobs,
      reedUk,
      seekAus
    )(Concat(_))

}
