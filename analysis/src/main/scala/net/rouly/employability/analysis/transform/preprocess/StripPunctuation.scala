package net.rouly.employability.analysis.transform.preprocess

import net.rouly.employability.models.Document

private[transform] object StripPunctuation extends (Document[String] => Document[String]) {

  override def apply(document: Document[String]): Document[String] = {
    Document(
      id = document.id,
      raw = document.raw,
      content = document.content
        // Replace fancy quotes.
        .replaceAll("[\\u2018\\u2019]", "'")
        .replaceAll("[\\u201C\\u201D]", "\"")
        .map(replaceIllegal)
    )
  }

  private def replaceIllegal(c: Char): Char =
    if (isLegal(c)) c
    else ' '

  private def isLegal(c: Char): Boolean = {
    val isAlpha = Character.isAlphabetic(c)
    val isSpace = Character.isWhitespace(c)
    isAlpha || isSpace
  }

}
