package com.clearcut.nlp

import play.api.libs.json.{JsObject, JsString, JsValue, Json}

import scala.io.Source

class JSONReader(input:Source,
                 idKey:String, documentKey:String)
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

      val jsObj = Json.parse(line).asInstanceOf[JsObject]

      val maybeDocumentId = jsObj.value.get(idKey).map(_.asInstanceOf[JsValue].toString)
      val maybeDocumentStr = jsObj.value.get(documentKey).map(_.asInstanceOf[JsString].value)

      (maybeDocumentId, maybeDocumentStr) match {
        case (Some(documentId:String), Some(documentStr:String)) =>
          n = (documentId, documentStr)
        case (None,_) =>
          System.err.println(s"Warning: skipped malformed line ${num}: ${line}")
        case (_, None) =>
          System.err.println(s"Warning: skipped malformed line ${num}: ${line}")
      }
    }
    n
  }
}