package com.clearcut.pipe.annotator

import com.clearcut.pipe.model._
import scala.collection.JavaConversions._
import edu.stanford.nlp.ling.CoreAnnotations
import edu.stanford.nlp.pipeline.{Annotation => StAnnotation, AnnotatorFactories, StanfordHelper}
import java.util._

/** Wraps CoreNLP POS Tagger as an Annotator. */
class StanfordPOSTagger extends Annotator(
      generates = Array(classOf[Poss]),
      requires = Array(classOf[Text], classOf[TokenOffsets], classOf[Tokens], classOf[SentenceOffsets])) {
  
  val properties = new Properties()
  //@transient lazy val stanfordAnnotator = StanfordHelper.getAnnotator(properties, "pos")
  @transient lazy val stanfordAnnotator =
    AnnotatorFactories.posTag(properties, StanfordHelper.getAnnotatorImplementations).create()

  override def annotate(ins:AnyRef*):Array[AnyRef] = {
    Array(run(ins(0).asInstanceOf[Text],
        ins(1).asInstanceOf[TokenOffsets],
        ins(2).asInstanceOf[Tokens],
        ins(3).asInstanceOf[SentenceOffsets]))
  }
  
  def run(t:Text, toa:TokenOffsets, to:Tokens, soa:SentenceOffsets):Poss = {
    val stanAnn = new edu.stanford.nlp.pipeline.Annotation(t.text)
    StanfordTokenizer.toStanford(t, toa, to, stanAnn)
    StanfordSentenceSplitter.toStanford(soa, null, stanAnn)

    stanfordAnnotator.annotate(stanAnn)

    StanfordPOSTagger.fromStanford(stanAnn)
  }
}

/** Stanford model mappings for POS tags. */
object StanfordPOSTagger {
  def toStanford(from:Poss, to:StAnnotation):Unit = {
    val li = to.get(classOf[CoreAnnotations.TokensAnnotation])
    for (i <- 0 until li.size) {
      val pos = from.pos(i)
      li.get(i).set(classOf[CoreAnnotations.PartOfSpeechAnnotation], pos)
    }
  }

  def fromStanford(from:StAnnotation):Poss = {
    val tokens = from.get(classOf[CoreAnnotations.TokensAnnotation])
    Poss(tokens.map(_.getString(classOf[CoreAnnotations.PartOfSpeechAnnotation])).toArray)
  }
}
