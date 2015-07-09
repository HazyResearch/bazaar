package com.clearcut.pipe.io

import java.io.{File, OutputStreamWriter, FileOutputStream, BufferedWriter}
import org.json4s._
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization.{read, write}
import com.clearcut.pipe.model.{Util, Tokens}

import com.clearcut.pipe.Schema

class ColumnWriter(dir:String) extends Writer {

  var writers:Array[BufferedWriter] = null

  def setSchema(schema:Schema): Unit = {
    writers = schema.annTyps.map(t => {
      val name = dir + "/ann." + Util.clazz2name(t)
      if (new File(name).exists)
        null
      else
        new BufferedWriter(
          new OutputStreamWriter(new FileOutputStream(name)))
    })
  }

  def write(annotations:Seq[AnyRef]) = {
    for (i <- 0 until writers.length) {
      if (writers(i) != null) {
        val json = Json.write(annotations(i))
        writers(i).write(json)
        writers(i).newLine
      }
    }
  }

  def close =
    for (w <- writers)
      if (w != null) w.close
}
