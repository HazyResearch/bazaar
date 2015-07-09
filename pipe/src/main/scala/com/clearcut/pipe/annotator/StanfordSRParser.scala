package com.clearcut.pipe.annotator

// StanfordSRParser is very fast, but needs A LOT of memory
// ~ 4GB per thread
// with less memory it becomes very slow

import java.util.Properties

import com.clearcut.pipe.model._
import edu.stanford.nlp.ling.CoreAnnotations.{SentenceIndexAnnotation, SentencesAnnotation}
import edu.stanford.nlp.pipeline.{Annotation => StAnnotation, StanfordHelper, AnnotatorFactories}
import edu.stanford.nlp.trees.Tree
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation

import scala.collection.JavaConversions._

class StanfordSRParser extends Annotator(
	generates = Array(classOf[Parses]),
	requires = Array(classOf[Text], classOf[SentenceOffsets], classOf[SentenceTokenOffsets],
		classOf[TokenOffsets], classOf[Tokens], classOf[Poss])) {

  val properties = new Properties()
  properties.setProperty("annotators", "tokenize,ssplit")
  //properties.put("parse.maxlen", conf.maxSentenceLength)
  properties.put("parse.model", "edu/stanford/nlp/models/srparser/englishSR.ser.gz")
  properties.put("threads", "1") // Should use extractor-level parallelism


  //@transient lazy val stanfordAnnotator = StanfordHelper.getAnnotator(properties, "tokenize")

  @transient lazy val stanfordAnnotator =
    AnnotatorFactories.parse(properties, StanfordHelper.getAnnotatorImplementations).create()


//  val modelPath = "edu/stanford/nlp/models/srparser/englishSR.ser.gz"
//	val taggerPath = "edu/stanford/nlp/models/pos-tagger/english-left3words/english-left3words-distsim.tagger"
//
//	val properties = new Properties()
//
//	@transient lazy val tagger = new MaxentTagger(taggerPath)
//	@transient lazy val model = ShiftReduceParser.loadModel(modelPath)

	override def annotate(ins:AnyRef*):Array[AnyRef] = {
		Array(run(ins(0).asInstanceOf[Text],
			ins(1).asInstanceOf[SentenceOffsets],
			ins(2).asInstanceOf[SentenceTokenOffsets],
			ins(3).asInstanceOf[TokenOffsets],
			ins(4).asInstanceOf[Tokens],
      ins(5).asInstanceOf[Poss]))
	}

	def run(t:Text, soa:SentenceOffsets, stoa:SentenceTokenOffsets, toa:TokenOffsets, to:Tokens, poa:Poss):
	  (Parses,SentenceDependencies) = {

    // create Stanford annotation with relevant contents
    val stanAnn = new StAnnotation(t.text)
    StanfordTokenizer.toStanford(t, toa, to, stanAnn)
    StanfordSentenceSplitter.toStanford(soa, null, stanAnn)
    StanfordPOSTagger.toStanford(poa, stanAnn)

    // stanford parser may take too long for all sentences of a document
    // we must parse sentence by sentence and then report progress using
    //if (reporter != null) reporter.incrementCounter();

    // run stanford annotator
    stanfordAnnotator.annotate(stanAnn)

    // put output back into our annotation
    val pa = StanfordSRParser.fromStanford(stanAnn)

    // get dependencies
    val da = StanfordDependencyExtractor.fromStanford(stanAnn)
    (pa, da)


//
//
//    //val stanAnn = new StAnnotation(t.text)
//
//		//StanfordDocumentPreprocessor.toStanford(t, toa, to, soa, stoa, stanAnn)
//
//		// stanford parser may take too long for all sentences of a document
//		// we must parse sentence by sentence and then report progress using
//		// if (reporter != null) reporter.incrementCounter();
//
//		val sentences = stanAnn.get(classOf[CoreAnnotations.SentencesAnnotation])
//		val l = for (sentence <- sentences) yield {
//			val s = sentence.get(classOf[CoreAnnotations.TokensAnnotation])
//			val tagged = tagger.tagSentence(s)
//			model.apply(tagged)
//		}
//
//		// get phrase structure trees
//		val ls = for (tree <- l) yield {
//			if (tree != null) tree.pennString else null
//		}
//		val pa = ParseAnn(ls.toArray)
//
//		// get dependencies
//		val da = StanfordDependencyExtractor.fromStanford(stanAnn)
//		(pa, da)
	}
}

object StanfordSRParser {
  def toStanford(from:Parses, to:StAnnotation):Unit = {
		val l = from.sents
		val sentences = to.get(classOf[SentencesAnnotation])
		for (i <- 0 until l.size) {
			var tree:Tree = null
			if (l(i) != null)
				tree = Tree.valueOf(l(i))
			sentences.get(i).set(classOf[TreeAnnotation], tree)
			sentences.get(i).set(classOf[SentenceIndexAnnotation], i.asInstanceOf[Integer])
		}
  }
  
  def fromStanford(from:StAnnotation):Parses = {
		val sentences = from.get(classOf[SentencesAnnotation])
		val l = for (sentence <- sentences) yield {
			val tree = sentence.get(classOf[TreeAnnotation])
			if (tree != null) tree.pennString else null
		}
		Parses(l.toArray)
  }
}
