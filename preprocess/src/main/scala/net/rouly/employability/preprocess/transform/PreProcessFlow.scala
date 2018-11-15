package net.rouly.employability.preprocess.transform

import akka.NotUsed
import akka.stream.scaladsl.Flow
import net.rouly.employability.models.Document
import net.rouly.employability.preprocess.opennlp.AnalysisOpenNlpModels
import net.rouly.employability.preprocess.transform.preprocess.FilterEnglish._
import net.rouly.employability.preprocess.transform.preprocess.StripPunctuation
import opennlp.tools.langdetect.{LanguageDetector, LanguageDetectorME}
import opennlp.tools.stemmer.{PorterStemmer, Stemmer}
import opennlp.tools.tokenize._
import opennlp.tools.util.normalizer._
object PreProcessFlow {

  def apply(
    tokenizer: Tokenizer,
    languageDetector: LanguageDetector,
    normalizer: CharSequenceNormalizer,
    stemmer: Stemmer
  ): Flow[Document[String], Document[String], NotUsed] = {

    Flow[Document[String]]
      .filter(languageDetector.isEnglish) // only allow English sentences
      .map(StripPunctuation) // strip out non-letters
      .map(lift(_.toLowerCase.trim)) // consistently lowercase all words
      .map(lift(tokenizer.tokenize)) // split sentences into tokens
      .map(lift(_.map(normalizer.normalize))) // normalize sequences
      .map(lift(_.filter(_.length > 3))) // drop short tokens
      .map(lift(_.map(stemmer.stem))) // reduce tokens to stems
      .map(lift(_.toSeq.map(_.toString))) // reshape as Seq[String]
      .map(lift(_.mkString("|"))) // concatenate tokens with a known delimiter

  }

  def apply(models: AnalysisOpenNlpModels): Flow[Document[String], Document[String], NotUsed] = {
    val tokenizer = new TokenizerME(models.tokenizerModel)
    val languageDetector = new LanguageDetectorME(models.languageDetector)
    val normalizer = new ShrinkCharSequenceNormalizer
    val stemmer = new PorterStemmer
    apply(tokenizer, languageDetector, normalizer, stemmer)
  }

  /**
    * Lift a function up into [[Document]] and apply it to [[Document.content]].
    */
  private def lift[T, R](fn: T => R): Document[T] => Document[R] =
    dt => Document(id = dt.id, raw = dt.raw, content = fn(dt.content))

}
