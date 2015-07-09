package com.clearcut.pipe.model

case class Offsets(f:Int, t:Int) {
  override def toString:String =
    f + "-" + t
}