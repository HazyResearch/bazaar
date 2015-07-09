package com.clearcut.pipe.model

case class SentenceOffsets(
  sents:Array[Offsets] = Array()
) {
  override def toString:String =
    "SentenceOffsets(" + sents.mkString(",") + ")"
}

//case class Sentence(
//  documentID:Int,
//  sentNum:Int,
//  charOffsets:Offsets,
//  tokOffsets:Offsets
//)