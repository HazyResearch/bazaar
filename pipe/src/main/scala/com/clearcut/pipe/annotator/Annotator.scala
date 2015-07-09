package com.clearcut.pipe.annotator

class Annotator(
                 val generates:Array[Class[_ <: AnyRef]],
                 val requires:Array[Class[_ <: AnyRef]]) extends java.io.Serializable {

  def annotate(ins:AnyRef*):Array[AnyRef] = ???

  def init = {}

  def close = {}

//  protected var reporter:ProgressReporter = null
//
//  protected def setProgressReporter(reporter:ProgressReporter) = {
//    this.reporter = reporter
//  }
}
