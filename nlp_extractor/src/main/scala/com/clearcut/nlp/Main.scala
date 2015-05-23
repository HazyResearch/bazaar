package com.clearcut.nlp

import java.io.{BufferedWriter, File, FileOutputStream, OutputStreamWriter}
import java.nio.charset.CodingErrorAction
import java.util.Properties

import scala.io.Source


object Main extends App {

  // Parse command line options
  case class Config(fileName:String, formatIn:String, documentKey: String, idKey: String, maxSentenceLength: String, //numThreads: String,
                    annotators: String = "tokenize, cleanxml, ssplit, pos, lemma, ner, parse")

  val parser = new scopt.OptionParser[Config]("DeepDive DocumentParser") {
    head("DocumentParser for TSV Extractors", "0.1")
    head("Input: stdin or a TSV file. The first column is document_id, the second column is the content of document.")
    head("Output: stdout or a TSV file named $inputfile.parsed")
    opt[String]('i', "formatIn") action { (x, c) =>
      c.copy(formatIn = x)
    } text("json or tsv")
    opt[String]('v', "valueKey") action { (x, c) =>
      c.copy(documentKey = x)
    } text("JSON key that contains the document, for example \"documents.text\"")
    opt[String]('k', "idKey") action { (x, c) =>
      c.copy(idKey = x)
    } text("JSON key that contains the document id, for example \"documents.id\"")
    opt[String]('l', "maxLength") action { (x, c) =>
      c.copy(maxSentenceLength = x)
    } text("Maximum length of sentences to parse (makes things faster) (default: 100)")
//    opt[String]('t', "numThreads") action { (x, c) =>
//      c.copy(numThreads = x)
//    } text("Number of threads (default: # of available cores)")
    opt[String]('a', "annotators") action { (x, c) =>
      c.copy(annotators = x)
    } text("CoreNLP annotators (default: 'tokenize,cleanxml,ssplit,pos,lemma', minimum: 'tokenize,ssplit')")
    opt[String]('f', "file") action { (x, c) =>
      c.copy(fileName = x)
    } text("Input file name")
  }

  val conf = parser.parse(args, Config(null, "json", "text", "id", "100", null)) getOrElse {
    throw new IllegalArgumentException
  }

  System.err.println(s"Parsing with max_len=${conf.maxSentenceLength}")

  // Configuration has been parsed, execute the Document parser
  val props = new Properties()
  props.put("annotators", "tokenize, cleanxml, ssplit, pos, lemma, ner, parse")
  props.put("parse.maxlen", conf.maxSentenceLength)
  props.put("parse.model", "edu/stanford/nlp/models/srparser/englishSR.ser.gz")
  props.put("threads", "1") // Should use extractor-level parallelism
  props.put("clean.allowflawedxml", "true")
  props.put("clean.sentenceendingtags", "p|br|div|li|ul|ol|h1|h2|h3|h4|h5|blockquote|section|article")
  val dp = new DocumentParser(props)

  implicit val codec = new scala.io.Codec(
    java.nio.charset.Charset.forName("utf-8"))
  codec.onMalformedInput(CodingErrorAction.IGNORE)
  codec.onUnmappableCharacter(CodingErrorAction.IGNORE)

  // Read each json object from stdin or file and parse the document
  var input = Source.stdin
  var output: BufferedWriter = null
  var errout: BufferedWriter = null
  if (conf.fileName != null) {
    if (!new File(conf.fileName).exists) {
      System.err.println("Input file does not exist: " + conf.fileName)
      System.exit(1)
    }
    input = Source.fromFile(conf.fileName)
    val outputFile = new File(conf.fileName + ".parsed")
    System.err.println("Writing to file: " + conf.fileName + ".parsed")
    output = new BufferedWriter(
      new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8"),
      1000 * 1000
    )
  }
  var reader:Iterator[(String,String)] = if (conf.formatIn.equals("json"))
    new JSONReader(input, "id", "text")
  else
    new TSVReader(input, 0, 1)

  reader.foreach { case (documentId, documentStr) =>



//  }
//
//
//  input.getLines.zipWithIndex.foreach { case(line, idx) =>
//



//    val tsvArr = line.trim.split("\t")
//    if (tsvArr.length >= 2) // skip malformed lines
//    {
//      val documentId = tsvArr(0)
//      val documentStr = tsvArr(1)
  
      System.err.println(s"Parsing document ${documentId}...")
 
      try { 
        // Output a TSV row for each sentence
        dp.parseDocumentString(documentStr).sentences.zipWithIndex
            .foreach { case (sentenceResult, sentence_idx) =>
  
          if (documentId != "") {
            val outline = List(
              documentId,
              sentence_idx + 1,
              sentenceResult.sentence,
              dp.list2TSVArray(sentenceResult.words),
              dp.list2TSVArray(sentenceResult.lemma),
              dp.list2TSVArray(sentenceResult.pos_tags),
              dp.list2TSVArray(sentenceResult.ner_tags),
              dp.intList2TSVArray(sentenceResult.offsets),
              dp.list2TSVArray(sentenceResult.dep_labels),
              dp.intList2TSVArray(sentenceResult.dep_parents)
              // dp.list2TSVArray(sentenceResult.collapsed_deps)
            ).mkString("\t")
            if (output != null) {
              output.append(outline)
              output.newLine()
            } else {
              Console.println(outline)
            }
          }
        }
      } catch {
        case e:Exception => 
           val erroutFile = new File(conf.fileName + ".failed")
           errout = new BufferedWriter(
             new OutputStreamWriter(new FileOutputStream(erroutFile), "UTF-8"),
             4096
           )
           //errout.write(s"Warning: skipped line ${idx} due to error in corenlp: ${line}\n")
           e.printStackTrace(new java.io.PrintWriter(errout))
      }
//    } else {
//      System.err.println(s"Warning: skipped malformed line ${idx}: ${line}")
//    }
  }

  if (output != null) {
    output.flush()
    output.close()
  }
  if (errout != null) {
    errout.flush()
    errout.close()
  }
}
