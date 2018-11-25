package net.rouly.employability.preprocess.transform

import akka.NotUsed
import akka.stream.scaladsl.Flow
import net.rouly.employability.models.Document
import net.rouly.employability.preprocess.opennlp.AnalysisOpenNlpModels
import net.rouly.employability.preprocess.transform.preprocess.FilterEnglish._
import net.rouly.employability.preprocess.transform.preprocess.{GarbageRemover, StopWordRemover}
import opennlp.tools.langdetect.LanguageDetectorME
import opennlp.tools.stemmer.PorterStemmer
import opennlp.tools.tokenize._
import opennlp.tools.util.normalizer._

object PreProcessFlow {

  def apply(models: AnalysisOpenNlpModels): Flow[Document[String], Document[String], NotUsed] = {
    val tokenizer = new TokenizerME(models.tokenizerModel)
    val languageDetector = new LanguageDetectorME(models.languageDetector)
    val normalizer = new ShrinkCharSequenceNormalizer
    val stemmer = new PorterStemmer

    Flow[Document[String]]
      .filter(languageDetector.isEnglish) // only allow English sentences
      .map(lift(_.toLowerCase.trim)) // consistently lowercase all words
      .map(lift(tokenizer.tokenize)) // split sentences into tokens
      .map(lift(_.filterNot(StopWordRemover.isStopWord))) // remove stop words
      .map(lift(_.filterNot(GarbageRemover.isGarbage))) // remove tokens with non-word characters
      .map(lift(_.map(normalizer.normalize))) // normalize sequences
      .map(lift(_.filter(_.length > 3))) // drop short tokens
      .map(lift(_.map(stemmer.stem))) // reduce tokens to stems
      .map(lift(_.toSeq.map(_.toString))) // reshape as Seq[String]
      .filter(_.content.distinct.length < 15) // drop short documents
      .map(lift(_.mkString("|"))) // concatenate tokens with a known delimiter
  }

  /**
    * Lift a function up into [[Document]] and apply it to [[Document.content]].
    */
  private def lift[T, R](fn: T => R): Document[T] => Document[R] =
    dt => Document(id = dt.id, raw = dt.raw, content = fn(dt.content))

}
