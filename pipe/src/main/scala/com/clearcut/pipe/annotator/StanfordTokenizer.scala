package com.clearcut.pipe.annotator

import java.util.{ArrayList, Properties}
import com.clearcut.pipe.model.{Offsets, Text, Tokens, TokenOffsets}
import edu.stanford.nlp.ling.{CoreAnnotations, CoreLabel}
import edu.stanford.nlp.pipeline.{Annotation => StAnnotation, AnnotatorFactories, StanfordHelper}
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

/** Wraps CoreNLP Tokenizer as an Annotator. */
class StanfordTokenizer extends Annotator(
  generates = Array(classOf[TokenOffsets], classOf[Tokens]),
  requires = Array(classOf[Text])) {

  val properties = new Properties()

  @transient lazy val stanfordAnnotator =
    AnnotatorFactories.tokenize(properties, StanfordHelper.getAnnotatorImplementations).create()

  override def annotate(ins:AnyRef*):Array[AnyRef] = {
    println(ins(0))
    val t = run(ins(0).asInstanceOf[Text])
    Array(t._1, t._2)
  }

  def run(t:Text):(TokenOffsets, Tokens) = {
    val stanAnn = new StAnnotation(t.text)
    stanfordAnnotator.annotate(stanAnn)
    StanfordTokenizer.fromStanford(stanAnn)
  }
}

/** Stanford model mappings for tokens. */
object StanfordTokenizer {
  def toStanford(fromText:Text, fromTokenOff:TokenOffsets, fromToken:Tokens, to:StAnnotation):Unit = {
    val text = fromText.text
    val tokenOffs = fromTokenOff.tokens
    val tokens = fromToken.tokens
    val li = for (i <- 0 until tokens.size) yield {
      val to = tokenOffs(i)
      val cl = new CoreLabel
      cl.setValue(tokens(i))
      cl.setWord(tokens(i))
      cl.setOriginalText(text.substring(to.f, to.t))
      cl.set(classOf[CoreAnnotations.CharacterOffsetBeginAnnotation], to.f.asInstanceOf[Integer])
      cl.set(classOf[CoreAnnotations.CharacterOffsetEndAnnotation], to.t.asInstanceOf[Integer])
      cl
    }
    to.set(classOf[CoreAnnotations.TokensAnnotation], li.asJava)
  }

  def fromStanford(from:StAnnotation):(TokenOffsets, Tokens) = {
    val tokens = from.get(classOf[CoreAnnotations.TokensAnnotation])
    val li = tokens.map(cl => Offsets(cl.beginPosition, cl.endPosition))
    val ti = tokens.map(_.word)
    (TokenOffsets(li.toArray), Tokens(ti.toArray))
  }
}