package com.clearcut.nlp

import scala.io.BufferedSource

class TSVReader(input:BufferedSource,
                 idCol:Int, documentCol:Int)
  extends Iterator[(String,String)] {

  var it = input.getLines.zipWithIndex
  var _next = fetchNext()

  override def hasNext: Boolean =
    _next != null

  override def next(): (String, String) = {
    val n = _next
    _next = fetchNext()
    n
  }

  private def fetchNext(): (String,String) = {
    var n:(String,String) = null
    while (n == null && it.hasNext) {
      val (line, num) = it.next
      val tsvArr = line.trim.split("\t")
      if (tsvArr.length >= 2) {
        val documentId = tsvArr(0)
        val documentStr = tsvArr(1)
        n = (documentId, documentStr)
      } else {
        System.err.println(s"Warning: skipped malformed line ${num}: ${line}")
      }
    }
    n
  }
}