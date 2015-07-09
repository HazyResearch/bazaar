package com.clearcut.pipe.model

case class Ners(
  entities:Array[NamedEntity] = Array()
) {
  override def toString = 
    "NERs(" + entities.mkString(",")  + ")"
}

case class NamedEntity(
  typ:String,
  offsets:Offsets,
  head:Int = -1
) {
  override def toString =
    typ + ":" + offsets + ":" + head
}