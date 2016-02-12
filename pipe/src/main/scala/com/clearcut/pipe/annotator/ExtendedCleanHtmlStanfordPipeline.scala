package com.clearcut.pipe.annotator

import scala.collection.JavaConversions._
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation
import edu.stanford.nlp.pipeline.{StanfordCoreNLP, Annotation}
import java.util.Properties
import com.clearcut.pipe.model._
import java.util.regex._
import org.jsoup.Jsoup
import org.jsoup.safety._

class ExtendedCleanHtmlStanfordPipeline extends Annotator[(Text), (Html, SentenceOffsets, TokenOffsets, Tokens, Poss, NerTags, Lemmas,
  SentenceDependencies, Parses, TrueCases, SentenceTokenOffsets)] {

  val props = new Properties()
  props.put("annotators", "tokenize, cleanxml, ssplit, pos, lemma, ner, parse, truecase")
  props.put("clean.xmltags", ".*")
  props.put("parse.maxlen", "100")
  props.put("parse.model", "edu/stanford/nlp/models/srparser/englishSR.ser.gz")
  props.put("parse.originalDependencies", "true")
  props.put("truecase.model", "edu/stanford/nlp/models/truecase/truecasing.fast.qn.ser.gz")
  props.put("threads", "1") // Should use extractor-level parallelism
  props.put("clean.allowflawedxml", "true")
  props.put("clean.sentenceendingtags", "p|br|div|li|ul|ol|h1|h2|h3|h4|h5|blockquote|section|article")

  @transient lazy val pipeline = new StanfordCoreNLP(props)

  val stripHtml = Pattern.compile("<\\/?a|A[^>]*>")

  override def annotate(t:Text):(Html, SentenceOffsets, TokenOffsets, Tokens, Poss, NerTags, Lemmas, SentenceDependencies, Parses, TrueCases, SentenceTokenOffsets) = {
    var text = extractCleanHtml(t)
    //var text = t


    //var text = extractText(t)
    //println(text)

    // Temporary fix for bug where brackets are being incorrectly treated as punct
    // and somehow this messes up the whole dep parse -> change them to round braces
    text = text.replaceAll( """\[""", "(").replaceAll( """\]""", ")")

    /*
    // workaround for a CoreNLP bug, not sure why some <a href=...> </a> tags
    // are not correctly handled by cleanxml
    val m = stripHtml.matcher(text)
    val ca = text.toCharArray()
    while (m.find()) {
        for (i <- m.start() until m.end())
            ca(i) = ' '
    }
    text = new String(ca)
    */

    var stanAnn = new Annotation(text)
    try {
      pipeline.annotate(stanAnn)
    
    } catch {
      // Even if we clean up the HTML tags with beautifulsoup (python) before
      // running this annotator, we sometimes observe an error that one tag is
      // closed, but opened. In this case, we can clean with jsoup, and it works.

      case e:Exception =>
         System.err.println(text)
         e.printStackTrace(System.err)
         System.err.flush()
         /*
         if (e.getMessage() != null && 
             e.getMessage().startsWith("Got a close tag")) {
            text = extractCleanHtml(text)
            //text = text.replaceAll("<[^>]+>", "")
            stanAnn = new Annotation(text)
            pipeline.annotate(stanAnn)
         } else
            throw e
         */
         return (text, Array[Offsets](), Array[Offsets](), Array[String](),           Array[String](), Array[String](), Array[String](), Array[Array[Dependency]](), Array[String](), Array[String](), Array[Offsets]())  
    }

    val (toa, to) = StanfordTokenizer.fromStanford(stanAnn)
    val poss = StanfordPOSTagger.fromStanford(stanAnn)
    val nertags = StanfordNERTagger.fromStanford(stanAnn)
    val lemmas = StanfordLemmatizer.fromStanford(stanAnn)
    val deps = StanfordDependencyExtractor.fromStanford(stanAnn)
    val (so, sto) = StanfordSentenceSplitter.fromStanford(stanAnn)
    val pa = StanfordSRParser.fromStanford(stanAnn)
    val tcs = StanfordTrueCaseAnnotator.fromStanford(stanAnn)

    (text, so, toa, to, poss, nertags, lemmas, deps, pa, tcs, sto)
  }

  def extractText(s:String):String = {
    //Jsoup.parse(s).text()
    getText(Jsoup.parse(s))
  }

  def extractCleanHtml(html:String):String = {
    //Jsoup.clean(html, Whitelist.relaxed())
    //val doc = Jsoup.parse(html)
    val doc = Jsoup.parseBodyFragment(html).body()
    doc.html()
  }

  import org.jsoup.nodes._

  def getText(cell:Element):String = {
    var text:String = ""
    for (child <- cell.childNodes()) {
      if (child.isInstanceOf[TextNode]) {
        text += child.asInstanceOf[TextNode].getWholeText()
      } else if (child.isInstanceOf[Element]) {
        text += getText(child.asInstanceOf[Element])
      }
    }
    text
  }
}
