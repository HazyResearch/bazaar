package com.clearcut.pipe.model

case class Poss(
  pos:Array[String] = Array()  
) {
  override def toString = 
    "POSs(" + pos.mkString(",") + ")"
}