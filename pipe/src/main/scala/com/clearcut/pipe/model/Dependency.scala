package com.clearcut.pipe.model

case class Dependencies(
  dependencies:Array[Dependency] = Array()
) {
  override def toString:String =
    "Dependencies(" + dependencies.mkString(",") + ")"
} 

case class Dependency (
  name:String,
  from:Int,
  to:Int
) {
  override def toString:String =
    name + "-" + from + "-" + to
}

case class DependencyType(
  id:Int,
  name:String)