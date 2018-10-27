package net.rouly.employability.analysis.transform

import akka.NotUsed
import akka.stream.scaladsl.Flow
import net.rouly.employability.analysis.models.Document
import net.rouly.employability.analysis.opennlp.AnalysisOpenNlpModels
import net.rouly.employability.analysis.transform.preprocess._
import opennlp.tools.stemmer.{PorterStemmer, Stemmer}
import opennlp.tools.tokenize._
import opennlp.tools.util.normalizer._

object PreProcessFlow {

  def apply(
    tokenizer: Tokenizer,
    normalizer: CharSequenceNormalizer,
    stemmer: Stemmer
  ): Flow[Document[String], Document[Seq[String]], NotUsed] = {
    Flow.fromFunction(
      initialize
        andThen StripPunctuation // strip out non-letters
        andThen lift { _.toLowerCase.trim } // consistently lowercase all words
        andThen lift { tokenizer.tokenize } // split sentences into tokens
        andThen lift { _.map(normalizer.normalize) } // normalize sequences
        andThen lift { _.filter(_.length > 3) } // drop short tokens
        andThen lift { _.map(stemmer.stem) } // reduce tokens to stems
        andThen lift { _.toSeq.map(_.toString) } // reshape as Seq[String]
    )
  }

  def apply(models: AnalysisOpenNlpModels): Flow[Document[String], Document[Seq[String]], NotUsed] = {
    val tokenizer = new TokenizerME(new TokenizerModel(models.tokenizerModel.stream))
    val normalizer = new AggregateCharSequenceNormalizer(
      new NumberCharSequenceNormalizer,
      new ShrinkCharSequenceNormalizer
    )
    val stemmer = new PorterStemmer
    apply(tokenizer, normalizer, stemmer)
  }

  /**
    * Lift a function up into [[Document]] and apply it to [[Document.content]].
    */
  private def lift[T, R](fn: T => R): Document[T] => Document[R] =
    dt => Document(id = dt.id, content = fn(dt.content))

  private def initialize[T]: (Document[T] => Document[T]) = identity

}
