package com.clearcut.pipe.model

case class SentenceTokenOffsets(
  sents:Array[Offsets] = Array()
) {
  override def toString:String =
    "SentenceTokenOffsets(" + sents.mkString(",") + ")"
}