package net.rouly.employability.preprocess.transform.preprocess

import net.rouly.employability.models.Document
import opennlp.tools.langdetect.LanguageDetector

private[transform] object FilterEnglish {

  implicit class FilteringLanguageDetector(languageDetector: LanguageDetector) {
    def isEnglish(document: Document[String]): Boolean = FilterEnglish.isEnglish(languageDetector, document)
  }

  private def isEnglish(detector: LanguageDetector, document: Document[String]): Boolean = {

    val prediction = detector.predictLanguage(document.content)
    val confidence = prediction.getConfidence
    val language = prediction.getLang

    val isEnglish = language == "eng"
    val moderateConfidence = confidence > 0.90

    isEnglish && moderateConfidence

  }

}
