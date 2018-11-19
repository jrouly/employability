package net.rouly.employability.preprocess.transform.preprocess

object GarbageRemover {

  def isGarbage(token: String): Boolean = token.exists(isGarbage)

  def isGarbage(c: Char): Boolean = !Character.isLetter(c)

}
