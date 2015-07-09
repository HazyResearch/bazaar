package com.clearcut.pipe.model

case class TokenOffsets(
  tokens:Array[Offsets] = Array()
) {
  override def toString:String =
    "TokenOffsets(" + tokens.mkString(",") + ")"
}

case class Token(
  tokenID:Int,
  name:String
)