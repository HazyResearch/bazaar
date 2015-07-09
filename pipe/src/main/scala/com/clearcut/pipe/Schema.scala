package com.clearcut.pipe

import scala.collection.mutable.Map

case class Schema
(
  annTyps: Array[Class[_ <: AnyRef]] = Array(),
  defaults: Map[Class[_ <: AnyRef], Int] = Map(),
  provenance: Array[String] = Array()
)


object Schema {

  def defaultAnnotations(schema: Schema, needed: Array[Class[_ <: AnyRef]], all: Array[_ <: AnyRef]): Array[AnyRef] = {
    defaultAnnotationIndices(schema, needed).map(all(_))
  }

  def defaultAnnotationIndices(schema: Schema, needed: Array[Class[_ <: AnyRef]]): Array[Int] = {
    needed.map(schema.defaults(_))
  }

  def extendSchema(before: Schema, annotators: Array[com.clearcut.pipe.annotator.Annotator]): Schema = {
    val annTyps = Array.concat(before.annTyps, annotators.flatMap(_.generates))
    val defaults = Map[Class[_ <: AnyRef], Int]()
    defaults ++= before.defaults
    annTyps.zipWithIndex.foreach { case (c, i) => if (!defaults.contains(c)) defaults += (c -> i) }
    val provenance = Array.concat(before.provenance, annotators.flatMap(a => a.generates.map(_ => a.toString)))
    new Schema(annTyps, defaults, provenance)
  }

  def createSchema(annTyps: Class[_ <: AnyRef]*): Schema = {
    val defaults = Map[Class[_ <: AnyRef], Int]()
    annTyps.zipWithIndex.foreach { case (c, i) => if (!defaults.contains(c)) defaults += (c -> i) }
    val provenance = annTyps.toArray.map(_ => "provided")
    new Schema(annTyps.toArray, defaults, provenance)
  }

  def prettyPrint(s:Schema) = {
    s.annTyps.map(t => println(t.getName))
  }
}