package com.clearcut.pipe.annotator

import scala.collection.JavaConversions._
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation
import edu.stanford.nlp.pipeline.{StanfordCoreNLP, Annotation}
import java.util.Properties
import com.clearcut.pipe.model._

class ExtendedStanfordPipeline extends Annotator[(Text), (SentenceOffsets, TokenOffsets, Tokens, Poss, NerTags, Lemmas,
  SentenceDependencies, Parses, TrueCases)] {

  val props = new Properties()
  props.put("annotators", "tokenize, cleanxml, ssplit, pos, lemma, ner, parse, truecase")
  props.put("parse.maxlen", "100")
  props.put("parse.model", "edu/stanford/nlp/models/srparser/englishSR.ser.gz")
  props.put("threads", "1") // Should use extractor-level parallelism
  props.put("clean.allowflawedxml", "true")
  props.put("clean.sentenceendingtags", "p|br|div|li|ul|ol|h1|h2|h3|h4|h5|blockquote|section|article")

  @transient lazy val pipeline = new StanfordCoreNLP(props)

  override def annotate(t:Text):(SentenceOffsets, TokenOffsets, Tokens, Poss, NerTags, Lemmas, SentenceDependencies, Parses, TrueCases) = {
    // Temporary fix for bug where brackets are being incorrectly treated as punct
    // and somehow this messes up the whole dep parse -> change them to round braces
    val text = t.replaceAll( """\[""", "(").replaceAll( """\]""", ")")

    val stanAnn = new Annotation(text)
    pipeline.annotate(stanAnn)

    val (toa, to) = StanfordTokenizer.fromStanford(stanAnn)
    val poss = StanfordPOSTagger.fromStanford(stanAnn)
    val nertags = StanfordNERTagger.fromStanford(stanAnn)
    val lemmas = StanfordLemmatizer.fromStanford(stanAnn)
    val deps = StanfordDependencyExtractor.fromStanford(stanAnn)
    val (so, sto) = StanfordSentenceSplitter.fromStanford(stanAnn)
    val pa = StanfordSRParser.fromStanford(stanAnn)
    val tcs = StanfordTrueCaseAnnotator.fromStanford(stanAnn)

    (so, toa, to, poss, nertags, lemmas, deps, pa, tcs)
  }
}

