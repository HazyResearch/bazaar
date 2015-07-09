package com.clearcut.pipe.annotator

import java.util.Properties
import com.clearcut.pipe.model._
import scala.collection.JavaConversions.asScalaBuffer
import scala.collection.mutable.ArrayBuffer
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation
import edu.stanford.nlp.pipeline.{Annotation => StAnnotation, AnnotatorFactories, StanfordHelper}
;

/** Wraps CoreNLP Lemmatizer as an Annotator. */
class StanfordLemmatizer extends Annotator(
      generates = Array(classOf[Lemmas]),
      requires = Array(classOf[Text], classOf[Poss], classOf[SentenceOffsets],
          classOf[TokenOffsets], classOf[Tokens])) {
  
  val properties = new Properties()
  //@transient lazy val stanfordAnnotator = StanfordHelper.getAnnotator(properties, "lemma")
	@transient lazy val stanfordAnnotator =
		AnnotatorFactories.lemma(properties, StanfordHelper.getAnnotatorImplementations).create()

  override def annotate(ins:AnyRef*):Array[AnyRef] = {
    Array(run(ins(0).asInstanceOf[Text], ins(1).asInstanceOf[Poss],
        ins(2).asInstanceOf[SentenceOffsets], ins(3).asInstanceOf[TokenOffsets],
        ins(4).asInstanceOf[Tokens]))
  }
  
  def run(t:Text, poa:Poss, soa:SentenceOffsets, toa:TokenOffsets, to:Tokens):Lemmas = {
		val stanAnn = new StAnnotation(t.text)
		StanfordTokenizer.toStanford(t, toa, to, stanAnn)
		StanfordSentenceSplitter.toStanford(soa, null, stanAnn)
		StanfordPOSTagger.toStanford(poa, stanAnn)

		stanfordAnnotator.annotate(stanAnn)

		StanfordLemmatizer.fromStanford(stanAnn)
  }
}

/** Stanford model mappings for lemmas. */
object StanfordLemmatizer {
	def toStanford(from:Lemmas, to:StAnnotation):Unit = {
		val li = to.get(classOf[TokensAnnotation])
		for (i <- 0 until from.lemmas.size) {
			val lemma = from.lemmas(i)
			li.get(i).setLemma(lemma)
		}
	}

	def fromStanford(from:StAnnotation):Lemmas = {
		val tokens = from.get(classOf[TokensAnnotation])
		val li = for (cl <- tokens) yield {
			// there may be *NL* tokens outside sentences; the lemmatizer didn't reach
			// these, so set these manually to *NL*, so that serialization is OK
			var l = cl.lemma()
			if (l == null) l = "*NL*"
			l
		}
		Lemmas(li.toArray)
	}
}
