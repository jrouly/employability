package net.rouly.employability.postgres

import net.rouly.employability.models.JobPosting

class PostgresMapping() {

  implicit val jobPostingRequestBuilder: RecordInsertion[JobPosting] = {
  }

}
