package com.clearcut.pipe.model

case class Lemmas(
  lemmas:Array[String] = Array()
) {
  override def toString = 
    "Lemmas(" + lemmas.mkString(",") + ")"
}