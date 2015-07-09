package com.clearcut.pipe.model

case class Coreferences(
  chains:Array[CoreferenceChain] = Array()
) {
  override def toString = 
    "Coreferences(" + chains.mkString(",") + ")"
}

case class CoreferenceChain(
  chainNum:Int = -1,
  representativeMentionNum:Int = -1,
  mentionNums:Array[Int] = Array()
) {
  override def toString = 
    "[" + mentionNums.mkString(",") + "]"
}