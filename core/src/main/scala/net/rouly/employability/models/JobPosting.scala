package net.rouly.employability.models

import java.util.UUID

/**
  * @param id unique identifier
  * @param dataSet which data set the data came from
  * @param description body of the job description
  * @param title title of the job position, if given
  * @param skills specifically called out skills, if given
  */
case class JobPosting(
  id: UUID,
  dataSet: String,
  description: String,
  title: Option[String] = None,
  skills: Option[String] = None
)
