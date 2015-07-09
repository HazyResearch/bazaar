package com.clearcut.pipe.io

import java.nio.charset.CodingErrorAction

import com.clearcut.pipe.Schema
import com.clearcut.pipe.model.{Text, ID}

import scala.io.{Source, BufferedSource}

class TsvReader(in:String = null,
                idCol:Int = 0, documentCol:Int = 1,
                inSource:Source = null)
  extends Reader with Iterator[Array[AnyRef]] {

  implicit val codec = new scala.io.Codec(
    java.nio.charset.Charset.forName("utf-8"))
  codec.onMalformedInput(CodingErrorAction.IGNORE)
  codec.onUnmappableCharacter(CodingErrorAction.IGNORE)

  val reader = if (inSource != null) inSource else Source.fromFile(in)

  var it = reader.getLines.zipWithIndex
  var _next = fetchNext()

  override def getSchema:Schema =
    Schema.createSchema(classOf[ID], classOf[Text])

  override def hasNext: Boolean =
    _next != null

  override def next(): Array[AnyRef] = {
    val n = _next
    _next = fetchNext()
    n
  }

  private def fetchNext(): Array[AnyRef] = {
    var n:Array[AnyRef] = null
    while (n == null && it.hasNext) {
      val (line, num) = it.next
      val tsvArr = line.trim.split("\t")
      if (tsvArr.length >= 2) {
        val documentId = tsvArr(0)
        val documentStr = tsvArr(1)
        n = Array(ID(documentId), Text(documentStr))
      } else {
        System.err.println(s"Warning: skipped malformed line ${num}: ${line}")
      }
    }
    n
  }

  def close =
    reader.close
}