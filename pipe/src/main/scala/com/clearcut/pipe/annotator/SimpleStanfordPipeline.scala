package com.clearcut.pipe.annotator

import edu.stanford.nlp.pipeline.{StanfordCoreNLP, Annotation}
import java.util.Properties
import com.clearcut.pipe.model._

class SimpleStanfordPipeline extends Annotator(
  generates = Array(classOf[SentenceOffsets], classOf[TokenOffsets], classOf[Tokens], classOf[Poss], classOf[NerTags],
    classOf[Lemmas], classOf[SentenceDependencies]),
  requires = Array(classOf[Text])) {

  val props = new Properties()
  props.put("annotators", "tokenize, cleanxml, ssplit, pos, lemma, ner, parse")
  props.put("parse.maxlen", "100")
  props.put("parse.model", "edu/stanford/nlp/models/srparser/englishSR.ser.gz")
  props.put("threads", "1") // Should use extractor-level parallelism
  props.put("clean.allowflawedxml", "true")
  props.put("clean.sentenceendingtags", "p|br|div|li|ul|ol|h1|h2|h3|h4|h5|blockquote|section|article")

  @transient lazy val pipeline = new StanfordCoreNLP(props)

  override def annotate(ins:AnyRef*):Array[AnyRef] = {
    val t = run(ins(0).asInstanceOf[Text])
    Array(t._1, t._2)
  }

  def run(t:Text):(SentenceOffsets, TokenOffsets, Tokens, Poss, NerTags, Lemmas, SentenceDependencies) = {
    // Temporary fix for bug where brackets are being incorrectly treated as punct
    // and somehow this messes up the whole dep parse -> change them to round braces
    val text = t.text.replaceAll( """\[""", "(").replaceAll( """\]""", ")")

    val stanAnn = new Annotation(text)
    pipeline.annotate(stanAnn)

    val (toa, to) = StanfordTokenizer.fromStanford(stanAnn)
    val poss = StanfordPOSTagger.fromStanford(stanAnn)
    val nertags = StanfordNERTagger.fromStanford(stanAnn)
    val lemmas = StanfordLemmatizer.fromStanford(stanAnn)
    val deps = StanfordDependencyExtractor.fromStanford(stanAnn)
    val (so, sto) = StanfordSentenceSplitter.fromStanford(stanAnn)

    (so, toa, to, poss, nertags, lemmas, deps)
  }
}

