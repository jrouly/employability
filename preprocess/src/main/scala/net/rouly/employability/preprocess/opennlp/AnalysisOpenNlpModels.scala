package net.rouly.employability.preprocess.opennlp

class AnalysisOpenNlpModels(
  val languageDetector: OpenNlpModel,
  val placeNameModel: OpenNlpModel,
  val tokenizerModel: OpenNlpModel
)
