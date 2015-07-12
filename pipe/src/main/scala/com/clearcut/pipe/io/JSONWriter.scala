package com.clearcut.pipe.io

import java.io.{OutputStreamWriter, FileOutputStream, BufferedWriter}

import com.clearcut.pipe.Schema
import org.json4s._
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._

class JsonWriter(out:String) extends Writer {

  implicit val formats = DefaultFormats

  val writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(out)))

  def setSchema(schema:Schema): Unit = {}

  def write(annotations:Seq[AnyRef]) = {
    val arr:Seq[JObject] = annotations.map(x => JObject(JField(x.getClass.getName, Extraction.decompose(x))))
    var o:JObject = arr(0)
    for (i <- 1 until arr.length)
      o = o merge arr(i)

    writer.write(compact(render(o)))
    writer.newLine
  }

  def close =
    writer.close

}
