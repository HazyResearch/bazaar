package com.clearcut.pipe.model

case class Parses(
  sents:Array[String] = Array()
) {
  override def toString: String =
    "Parses(" + sents.mkString(",") + ")"
}