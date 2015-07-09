package com.clearcut.pipe.annotator

import java.util.ArrayList
import java.util.Properties

import com.clearcut.pipe.model._

import scala.collection.JavaConversions.asScalaBuffer
import scala.collection.mutable.ArrayBuffer
import scala.util.control.Breaks.break
import scala.util.control.Breaks.breakable

import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetBeginAnnotation
import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetEndAnnotation
import edu.stanford.nlp.ling.CoreAnnotations.SentenceIndexAnnotation
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation
import edu.stanford.nlp.ling.CoreAnnotations.TokenBeginAnnotation
import edu.stanford.nlp.ling.CoreAnnotations.TokenEndAnnotation
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation
import edu.stanford.nlp.ling.CoreLabel
import edu.stanford.nlp.pipeline.{Annotation => StAnnotation, AnnotatorFactories, StanfordHelper}
import edu.stanford.nlp.util.CoreMap

class StanfordSentenceSplitter extends Annotator(
	generates = Array(classOf[SentenceOffsets], classOf[SentenceTokenOffsets]),
	requires = Array(classOf[Text], classOf[TokenOffsets], classOf[Tokens])) {

	val properties = new Properties
	//@transient lazy val stanfordAnnotator = StanfordHelper.getAnnotator(properties, "ssplit")
	@transient lazy val stanfordAnnotator =
		AnnotatorFactories.sentenceSplit(properties, StanfordHelper.getAnnotatorImplementations).create()


	override def annotate(ins: AnyRef*): Array[AnyRef] = {
		val t = run(ins(0).asInstanceOf[Text],
			ins(1).asInstanceOf[TokenOffsets],
			ins(2).asInstanceOf[Tokens])
		Array(t._1, t._2)
	}

	def run(t: Text, td: TokenOffsets, to: Tokens): (SentenceOffsets, SentenceTokenOffsets) = {
		val sssFrags = new StanfordSentenceSplitterWithFrags
		val fa = TextFragments(Array(TextFragment("extract", Offsets(0, t.text.size), true)))
		sssFrags.run(t, fa, td, to)
	}
}

object StanfordSentenceSplitter {
  
  // second argument can be null, in which case we compute the token offsets
  def toStanford(sep:SentenceOffsets, stoa:SentenceTokenOffsets, to:StAnnotation) {
    val tokens = to.get(classOf[TokensAnnotation])
	val text = to.get(classOf[TextAnnotation])
	
	val sentences = new ArrayList[CoreMap]()
	var sentNum = 0
	var nextTok = 0
	for (i <- 0 until sep.sents.size) {
	  val s = sep.sents(i)
			
	  val sentenceText = text.substring(s.f, s.t)

	  var beginTok = -1
	  var endTok = -1
	  if (stoa != null) {
	    val sto = stoa.sents(i)
	    beginTok = sto.f
	    endTok = sto.t
	  } else {
	    while (nextTok < tokens.size && tokens.get(nextTok).beginPosition < s.f) nextTok += 1
	    beginTok = nextTok
	    endTok = beginTok
	    while (endTok < tokens.size && tokens.get(endTok).endPosition <= s.t) endTok += 1
	    nextTok = endTok
	  }
	  
	  val toks = to.get(classOf[TokensAnnotation]).subList(beginTok, endTok)
			
	  val sentence = new StAnnotation(sentenceText)
	  sentence.set(classOf[SentenceIndexAnnotation], sentNum.asInstanceOf[Integer])
	  sentence.set(classOf[CharacterOffsetBeginAnnotation], s.f.asInstanceOf[Integer])
	  sentence.set(classOf[CharacterOffsetEndAnnotation], s.t.asInstanceOf[Integer])
	  sentence.set(classOf[TokensAnnotation], toks)
	  sentence.set(classOf[TokenBeginAnnotation], beginTok.asInstanceOf[Integer])
	  sentence.set(classOf[TokenEndAnnotation], endTok.asInstanceOf[Integer])
	  sentences.add(sentence)
	  sentNum += 1
	}
	to.set(classOf[SentencesAnnotation], sentences)
  }
	
  def fromStanford(from:StAnnotation):(SentenceOffsets, SentenceTokenOffsets) = {
	val sentences = from.get(classOf[SentencesAnnotation])		
	val cli = new ArrayBuffer[Offsets](sentences.size)
	val tli = new ArrayBuffer[Offsets](sentences.size)
	for (sentence <- sentences) {
	  cli += Offsets(sentence.get(classOf[CharacterOffsetBeginAnnotation]), 
	      sentence.get(classOf[CharacterOffsetEndAnnotation]))
	      
	  tli += Offsets(sentence.get(classOf[TokenBeginAnnotation]), 
	      sentence.get(classOf[TokenEndAnnotation]))
	}
	(SentenceOffsets(cli.toArray), SentenceTokenOffsets(tli.toArray))
  }
}

// a sentence splitter that preserves frags boundaries
class StanfordSentenceSplitterWithFrags extends Annotator(
      generates = Array(classOf[SentenceOffsets], classOf[SentenceTokenOffsets]),
      requires = Array(classOf[Text], classOf[TextFragments], classOf[TokenOffsets], classOf[Tokens])) {

  val properties = new Properties()
  //@transient lazy val stanfordAnnotator = StanfordHelper.getAnnotator(properties, "ssplit")
	@transient lazy val stanfordAnnotator =
		AnnotatorFactories.sentenceSplit(properties, StanfordHelper.getAnnotatorImplementations).create()

  override def annotate(ins:AnyRef*):Array[AnyRef] = {
    val t = run(ins(0).asInstanceOf[Text],
        ins(1).asInstanceOf[TextFragments],
        ins(2).asInstanceOf[TokenOffsets],
        ins(3).asInstanceOf[Tokens])
    Array(t._1, t._2)
  }

  def run(t:Text, fa:TextFragments, td:TokenOffsets, to:Tokens):(SentenceOffsets, SentenceTokenOffsets) = {
	// create Stanford annotation with relevant contents
	val stanAnn = new StAnnotation(t.text)
	StanfordTokenizer.toStanford(t, td, to, stanAnn)

	val docSnts = new ArrayList[CoreMap]()
	val li = stanAnn.get(classOf[TokensAnnotation])
	var sentNum = 0

	// look at every fragment separately
	for (frag <- fa.frags) {
	  val raw = t.text.substring(frag.offsets.f, frag.offsets.t)

	  // get tokens annotations
	  val sli = new ArrayList[CoreLabel]()
	  var firstToken = -1
	  for (i <- 0 until li.size) {
		val cl = li.get(i);
		if (cl.get(classOf[CharacterOffsetBeginAnnotation]) >= frag.offsets.f &&
				cl.get(classOf[CharacterOffsetEndAnnotation]) <= frag.offsets.t) {
		  if (firstToken == -1) firstToken = i

		  val ncl = new CoreLabel()
		  ncl.setValue(cl.value)
		  ncl.setWord(cl.word)
		  ncl.setOriginalText(cl.originalText)
		  ncl.set(classOf[CharacterOffsetBeginAnnotation], new Integer(cl.beginPosition - frag.offsets.f))
		  ncl.set(classOf[CharacterOffsetEndAnnotation], new Integer(cl.endPosition - frag.offsets.f))
		  sli.add(ncl)
		}
	  }
	  val fragStanAnn = new StAnnotation(raw)

	  fragStanAnn.set(classOf[TokensAnnotation], sli)

	  // now run it
	  stanfordAnnotator.annotate(fragStanAnn)

	  for (sentence <- fragStanAnn.get(classOf[SentencesAnnotation])) {
		var sentenceTokens = sentence.get(classOf[TokensAnnotation])

		// 1. remove newlines at beginning or end of sentence
		var newStart = 0
		var newEnd = sentenceTokens.size
		breakable {
		  for (i <- 0 until sentenceTokens.size)
		    if (sentenceTokens.get(i).value().equals("*NL*")) newStart += 1 else break
		}
		breakable {
		  for (i <- sentenceTokens.size-1 to 0 by -1)
		    if (sentenceTokens.get(i).value().equals("*NL*")) newEnd -= 1 else break
		}

		// TODO: special case: no tokens left??
		if (newEnd > newStart) {
			//if (newStart == 0 && newEnd == sentenceTokens.size()) {
			//  snts.add(sentence);
			//  continue;
			//}
			//System.out.println(newStart)
			sentenceTokens = sentenceTokens.subList(newStart, newEnd)
			sentence.set(classOf[SentenceIndexAnnotation], sentNum.asInstanceOf[Integer])
			sentence.set(classOf[TokensAnnotation], sentenceTokens)
			sentence.set(classOf[TokenBeginAnnotation], new Integer(sentence.get(classOf[TokenBeginAnnotation]) + newStart))
			sentence.set(classOf[TokenEndAnnotation], new Integer(sentence.get(classOf[TokenBeginAnnotation]) + sentenceTokens.size))
			sentence.set(classOf[CharacterOffsetBeginAnnotation], new Integer(sentenceTokens.get(0).get(classOf[CharacterOffsetBeginAnnotation])))
			sentence.set(classOf[CharacterOffsetEndAnnotation], new Integer(sentenceTokens.get(sentenceTokens.size-1).get(classOf[CharacterOffsetEndAnnotation])))

			// 2. correct for document token offsets
			for (cl <- sentenceTokens) {
				cl.set(classOf[CharacterOffsetBeginAnnotation], new Integer(cl.get(classOf[CharacterOffsetBeginAnnotation]) + frag.offsets.f))
				cl.set(classOf[CharacterOffsetEndAnnotation], new Integer(cl.get(classOf[CharacterOffsetEndAnnotation]) + frag.offsets.f))
			}
			sentence.set(classOf[CharacterOffsetBeginAnnotation], new Integer(sentence.get(classOf[CharacterOffsetBeginAnnotation]) + frag.offsets.f))
			sentence.set(classOf[CharacterOffsetEndAnnotation], new Integer(sentence.get(classOf[CharacterOffsetEndAnnotation]) + frag.offsets.f))
			sentence.set(classOf[TokenBeginAnnotation], new Integer(sentence.get(classOf[TokenBeginAnnotation]) + firstToken))
			sentence.set(classOf[TokenEndAnnotation], new Integer(sentence.get(classOf[TokenEndAnnotation]) + firstToken))

			docSnts.add(sentence)
			sentNum += 1
		}
	  }
	  stanAnn.set(classOf[SentencesAnnotation], docSnts)
	}

	StanfordSentenceSplitter.fromStanford(stanAnn)
  }
}