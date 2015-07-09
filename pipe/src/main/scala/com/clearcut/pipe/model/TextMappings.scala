package com.clearcut.pipe.model

case class TextMappings(
  maps:Array[TextMapping] = Array()
)

case class TextMapping(
  documentID:Int,
  beginText:Int,
  beginSource:Int,
  length:Int
)