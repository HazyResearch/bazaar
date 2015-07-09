package com.clearcut.pipe.io

import java.io.{FileOutputStream, OutputStreamWriter, BufferedWriter}

import com.clearcut.pipe.Schema
import com.clearcut.pipe.model._

/** Legacy writer for psql readable TSV table.
 *
 * Example output:
 * 12	1	This is a simple example.	{"This","is","a","simple","example","."}
 *   {"this","be","a","simple","example","."}	{"DT","VBZ","DT","JJ","NN","."}
 *   {"O","O","O","O","O","O"}	{0,5,8,10,17,24}
 *   {"nsubj","cop","det","amod","",""}	{5,5,5,5,0,0}
 */
class TsvWriter(out:String = null, outWriter:BufferedWriter = null) extends Writer {

  val writer = if (outWriter != null) outWriter else
    new BufferedWriter(new OutputStreamWriter(new FileOutputStream(out), "utf-8"))

  var indices:Array[Int] = null

  def setSchema(schema:Schema) = {
    indices = Schema.defaultAnnotationIndices(schema, Array(classOf[ID], classOf[Text], classOf[SentenceOffsets],
      classOf[SentenceTokenOffsets], classOf[Tokens], classOf[TokenOffsets], classOf[Lemmas], classOf[Poss],
      classOf[NerTags], classOf[SentenceDependencies]))
  }

  def write(annotations:Seq[AnyRef]) = {
    val is = indices.map(annotations(_))
    val id = is(0).asInstanceOf[ID]
    val ta = is(1).asInstanceOf[Text]
    val soa = is(2).asInstanceOf[SentenceOffsets]
    val stoa = is(3).asInstanceOf[SentenceTokenOffsets]
    val toka = is(4).asInstanceOf[Tokens]
    val toa = is(5).asInstanceOf[TokenOffsets]
    val la = is(6).asInstanceOf[Lemmas]
    val posa = is(7).asInstanceOf[Poss]
    val nertaga = is(8).asInstanceOf[NerTags]
    val sdepa = is(9).asInstanceOf[SentenceDependencies]

    for (sentNum <- 0 until soa.sents.size) {
      var columns = new Array[String](10)

      val s_stoa = stoa.sents(sentNum)

      val outline = List(
        id.id,
        sentNum.toString,
        ta.text.substring(soa.sents(sentNum).f, soa.sents(sentNum).t),
        list2TSVArray(toka.tokens.slice(s_stoa.f, s_stoa.t).toList),
        list2TSVArray(la.lemmas.slice(s_stoa.f, s_stoa.t).toList),
        list2TSVArray(posa.pos.slice(s_stoa.f, s_stoa.t).toList),
        list2TSVArray(nertaga.tokens.slice(s_stoa.f, s_stoa.t).toList),
        intList2TSVArray(toa.tokens.slice(s_stoa.f, s_stoa.t).map {_.f - soa.sents(sentNum).f }.toList),
        list2TSVArray(sdepa.sents(sentNum).map(_.name).toList),
        intList2TSVArray(sdepa.sents(sentNum).map(_.from).toList)
      )
      writer.append(outline.mkString("\t"))
      writer.newLine()
    }
  }

  /** Construct a Postgres-acceptable array in the TSV format, from a list */
  def list2TSVArray(arr: List[String]) : String = {
    return arr.map( x =>
      // Replace '\' with '\\\\' to be accepted by COPY FROM
      // Replace '"' with '\\"' to be accepted by COPY FROM
      if (x.contains("\\"))
        "\"" + x.replace("\\", "\\\\\\\\").replace("\"", "\\\\\"") + "\""
      else
        "\"" + x + "\""
    ).mkString("{", ",", "}")
  }

  def intList2TSVArray(arr: List[Int]) : String = {
    return arr.map( x =>
      "" + x
    ).mkString("{", ",", "}")
  }

  def string2TSVString(str: String) : String = {
    if (str.contains("\\"))
      str.replace("\\", "\\\\")
    else
      str
  }

  def close =
    writer.close
}