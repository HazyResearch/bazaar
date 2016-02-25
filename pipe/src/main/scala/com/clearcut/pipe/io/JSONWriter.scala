package com.clearcut.pipe.io

import java.io.{OutputStreamWriter, FileOutputStream, BufferedWriter}
import java.util.zip.GZIPOutputStream
import com.clearcut.pipe.model._

import com.clearcut.pipe.Schema
import org.json4s._
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._

class JsonWriter(out:String, compress:Boolean = false) extends Writer {

  implicit val formats = DefaultFormats

  val writer = compress match {
    case false => new BufferedWriter(new OutputStreamWriter(new FileOutputStream(out)))
    case true => new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(out))))
  }
  var names:Seq[String] = null

  def setSchema(schema:Schema): Unit = {
    names = schema.annTyps.map(t => lowerFirst(t))
  }

  def write(annotations:Seq[AnyRef]) = {
    val arr:Seq[JObject] = annotations.zip(names).map { case (x,n) => JObject(JField(n, Extraction.decompose(x)))}
    var o:JObject = arr(0)
    for (i <- 1 until arr.length)
      o = o merge arr(i)

    writer.write(compact(render(o)))
    writer.newLine
  }

  def close =
    writer.close

}
