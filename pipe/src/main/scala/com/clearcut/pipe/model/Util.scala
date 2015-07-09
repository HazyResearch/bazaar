package com.clearcut.pipe.model

object Util {

  val types:Array[Class[_ <: AnyRef]] = Array(
    classOf[Coreferences],
    classOf[Dependency],
    classOf[Lemmas],
    classOf[Mentions],
    classOf[Ners],
    classOf[NerTags],
    classOf[Offsets],
    classOf[Parses],
    classOf[Poss],
    classOf[SentenceDependencies],
    classOf[SentenceOffsets],
    classOf[SentenceTokenOffsets],
    classOf[Text],
    classOf[TextFragments],
    classOf[TextMappings],
    classOf[TokenOffsets],
    classOf[Tokens]
  )

  val name2clazz =
    Map(types.map(t => lowerFirst(t.getSimpleName) -> t):_*)

  val clazz2name:Map[Class[_ <: AnyRef], String] =
    name2clazz.map(_.swap)

  def lowerFirst(s:String) =
    if (s == null || s.length < 1) s
    else s.charAt(0).toLower + s.substring(1)
}
