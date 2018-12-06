package net.rouly.employability.web.application.model

case class BucketStats(
  title: String,
  labels: (String, String),
  buckets: List[BucketEntry]
)
