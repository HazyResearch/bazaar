package com.clearcut.pipe.model

case class SentenceDependencies(
  sents:Array[Array[Dependency]] = Array()
) {
  override def toString:String =
    "SentenceDependencies(" + sents.map(x => "[" + x.mkString(",") + "]").mkString(",") + ")"
} 
