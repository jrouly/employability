package net.rouly.employability.preprocess.opennlp

import opennlp.tools.langdetect.LanguageDetectorModel
import opennlp.tools.tokenize.TokenizerModel

class AnalysisOpenNlpModels(
  val languageDetector: LanguageDetectorModel,
  val tokenizerModel: TokenizerModel
)
