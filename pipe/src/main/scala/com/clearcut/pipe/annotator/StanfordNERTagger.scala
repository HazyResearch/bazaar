package com.clearcut.pipe.annotator

import com.clearcut.pipe.model._
import scala.collection.JavaConversions._
import edu.stanford.nlp.ling.CoreAnnotations
import edu.stanford.nlp.pipeline.{Annotation => StAnnotation, AnnotatorFactories, StanfordHelper}
import java.util._

/** Wraps CoreNLP NER Tagger as an Annotator. */
class StanfordNERTagger extends Annotator(
  generates = Array(classOf[NerTags]),
  requires = Array(classOf[Text], classOf[TokenOffsets], classOf[Tokens], classOf[SentenceOffsets],
    classOf[Lemmas], classOf[Poss])) {

  val properties = new Properties()
  //@transient lazy val stanfordAnnotator = StanfordHelper.getAnnotator(properties, "ner")
  @transient lazy val stanfordAnnotator =
    AnnotatorFactories.nerTag(properties, StanfordHelper.getAnnotatorImplementations).create()

  override def annotate(ins: AnyRef*): Array[AnyRef] = {
    Array(run(ins(0).asInstanceOf[Text],
      ins(1).asInstanceOf[TokenOffsets],
      ins(2).asInstanceOf[Tokens],
      ins(3).asInstanceOf[SentenceOffsets],
      ins(4).asInstanceOf[Lemmas],
      ins(5).asInstanceOf[Poss]))
  }

  def run(t: Text, toa: TokenOffsets, to: Tokens, soa: SentenceOffsets, la: Lemmas, pa: Poss):
    NerTags = {
    val stanAnn = new StAnnotation(t.text)
    StanfordTokenizer.toStanford(t, toa, to, stanAnn)
    StanfordSentenceSplitter.toStanford(soa, null, stanAnn)
    StanfordPOSTagger.toStanford(pa, stanAnn)
    StanfordLemmatizer.toStanford(la, stanAnn)

    stanfordAnnotator.annotate(stanAnn)

    StanfordNERTagger.fromStanford(stanAnn)
  }
}

/** Stanford model mappings for NER. */
object StanfordNERTagger {
  def toStanford(from:NerTags, to:StAnnotation):Unit = {
    val li = to.get(classOf[CoreAnnotations.TokensAnnotation])
    for (i <- 0 until li.size) {
      val ner = from.tokens(i)
      li.get(i).setNER(ner)
    }
  }
  
  def fromStanford(from:StAnnotation):NerTags = {
    val tokens = from.get(classOf[CoreAnnotations.TokensAnnotation])
    val li = for (cl <- tokens) yield {
      // there may be *NL* tokens outside sentences; the lemmatizer didn't reach
      // these, so set these manually to *NL*, so that serialization is OK
      val n = cl.ner
      if (n != null) n else "O"
    }
    NerTags(li.toArray)
  }
}
