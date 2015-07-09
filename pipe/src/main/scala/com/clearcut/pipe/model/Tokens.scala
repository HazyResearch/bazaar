package com.clearcut.pipe.model

case class Tokens(
  tokens:Array[String]
) {
  override def toString:String =
    "Tokens(" + tokens.mkString(",") + ")"
}