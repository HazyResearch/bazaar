package com.clearcut.pipe.model

case class TextFragments(
  frags:Array[TextFragment]
)

case class TextFragment (
  typ:String,
  offsets:Offsets,
  extract:Boolean
)