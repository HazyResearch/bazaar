package com.clearcut.pipe.model

case class NerTags(
  tokens:Array[String] = Array()
) {
  override def toString = 
    "NERTags(" + tokens.mkString(",") + ")"
}