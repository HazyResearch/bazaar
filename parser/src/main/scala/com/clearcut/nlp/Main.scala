package com.clearcut.nlp

import java.io.{BufferedWriter, File, FileOutputStream, OutputStreamWriter}
import java.nio.charset.CodingErrorAction
import java.util.Properties

import scala.io.Source


object Main extends App {

  // Parse command line options
  case class Config(serverPort: Integer = null,
                    fileName: String = null,
                    formatIn: String = "json",
                    documentKey: String = "text",
                    idKeys: String = "id",
                    maxSentenceLength: String = "100",
                    annotators: String = "tokenize, cleanxml, ssplit, pos, lemma, ner, parse")

  val optionsParser = new scopt.OptionParser[Config]("DeepDive DocumentParser") {
    head("DocumentParser for TSV Extractors", "0.1")
    head("Input: stdin or a TSV file. The first column is document_id, the second column is the content of document.")
    head("Output: stdout or a TSV file named $inputfile.parsed")
    opt[String]('i', "formatIn") action { (x, c) =>
      c.copy(formatIn = x)
    } text("json or tsv")
    opt[String]('v', "valueKey") action { (x, c) =>
      c.copy(documentKey = x)
    } text("JSON key that contains the document, for example \"documents.text\"")
    opt[String]('k', "idKeys") action { (x, c) =>
      c.copy(idKeys = x)
    } text("JSON keys that contains the document id and other fields, for example \"doc-id,section-id\"")
    opt[String]('l', "maxLength") action { (x, c) =>
      c.copy(maxSentenceLength = x)
    } text("Maximum length of sentences to parse (makes things faster) (default: 100)")
    opt[String]('a', "annotators") action { (x, c) =>
      c.copy(annotators = x)
    } text("CoreNLP annotators (default: 'tokenize,cleanxml,ssplit,pos,lemma', minimum: 'tokenize,ssplit')")
    opt[String]('f', "file") action { (x, c) =>
      c.copy(fileName = x)
    } text("Input file name")
    opt[Int]('p', "serverPort") action { (x, c) =>
      c.copy(serverPort = x)
    } text("Run as an HTTP service")
  }

  val conf = optionsParser.parse(args, Config()) getOrElse {
    throw new IllegalArgumentException
  }

  System.err.println(s"Parsing with max_len=${conf.maxSentenceLength}")

  // Configuration has been parsed, execute the Document parser
  val props = new Properties()
  props.put("annotators", conf.annotators)
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

  if (conf.serverPort != null) {
    Console.println("Listening on port " + conf.serverPort + "...")
    new Server(dp, conf.serverPort).run()
    System.exit(0)
  }


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

  // TODO: extend to handle tsv input again...
  val docIdKeys:Array[String] = conf.idKeys.split(",").map(_.trim)
  var reader:Iterator[(Array[String], String)] = new JSONReader(input,docIdKeys,conf.documentKey)
  reader.foreach { case (docIds, documentStr) =>
      System.err.println(s"Parsing document ${docIds(0)}...")
      try {
        // Output a TSV row for each sentence
        dp.parseDocumentString(documentStr).sentences.zipWithIndex
            .foreach { case (sentenceResult, sentence_idx) =>
          if (docIds(0) != "") {
            val idsOutline = docIds
            val mainOutline = List(
              sentence_idx + 1,
              dp.replaceChars(sentenceResult.sentence),
              dp.list2TSVArray(sentenceResult.words),
              dp.list2TSVArray(sentenceResult.lemma),
              dp.list2TSVArray(sentenceResult.pos_tags),
              dp.list2TSVArray(sentenceResult.ner_tags),
              dp.intList2TSVArray(sentenceResult.offsets),
              dp.list2TSVArray(sentenceResult.dep_labels),
              dp.intList2TSVArray(sentenceResult.dep_parents)
              // dp.list2TSVArray(sentenceResult.collapsed_deps)
            )
            val outline = (idsOutline ++ mainOutline).mkString("\t")
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
           errout.write(s"Warning: skipped line due to error in corenlp: ${documentStr}\n")
           e.printStackTrace(new java.io.PrintWriter(errout))
      }
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
