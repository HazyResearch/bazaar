package com.clearcut.pipe.model

case class Mentions(
  mentions:Array[Mention] = Array()
) {
  override def toString =
    "Mentions(" + mentions.mkString(",") + ")"
}

case class Mention(
  mentionNum:Int = -1,
  head:Int = -1,  // token offset from begin of document
  tokenOffsets:Offsets,
  mentionTyp:Byte = -1, //PRONOMINAL, NOMINAL, PROPER, UNKNOWN
  number:Byte = -1,     //SINGULAR, PLURAL, UNKNOWN
  gender:Byte = -1,     //MALE, FEMALE, NEUTRAL, UNKNOWN
  animacy:Byte = -1     //ANIMATE, INANIMATE, UNKNOWN
)

object Mention {
  val UNKNOWN = -1.toByte
  
  // mention types
  val PRONOMINAL = 0.toByte
  val NOMINAL = 1.toByte
  val PROPER = 2.toByte
  val LIST = 3.toByte
  
  // numbers
  val SINGULAR = 0.toByte
  val PLURAL = 1.toByte
  
  // genders
  val MALE = 0.toByte
  val FEMALE = 1.toByte
  val NEUTRAL = 2.toByte
  
  // animacy
  val ANIMATE = 0.toByte
  val INANIMATE = 1.toByte
  
  // need bidirectional mappings for stanford conversions

  def typeToByte(s:String) = s match {
    case "PRONOMINAL" => PRONOMINAL
    case "NOMINAL" => NOMINAL
    case "PROPER" => PROPER
    case "LIST" => LIST
    case "UNKNOWN" => UNKNOWN
  }
  
  def typeFromByte(b:Byte) = b match {
    case PRONOMINAL => "PRONOMINAL"
    case NOMINAL => "NOMINAL"
    case PROPER => "PROPER"
    case LIST => "LIST"
    case UNKNOWN => "UNKNOWN"
  }
  
  def numberToByte(s:String) = s match {
    case "SINGULAR" => SINGULAR
    case "PLURAL" => PLURAL
    case "UNKNOWN" => UNKNOWN
  }
  
  def numberFromByte(b:Byte) = b match {
    case SINGULAR => "SINGULAR"
    case PLURAL => "PLURAL"
    case UNKNOWN => "UNKNOWN"
  }

  def genderToByte(s:String) = s match {
    case "MALE" => MALE
    case "FEMALE" => FEMALE
    case "NEUTRAL" => NEUTRAL
    case "UNKNOWN" => UNKNOWN
  }
  
  def genderFromByte(b:Byte) = b match {
    case MALE => "MALE"
    case FEMALE => "FEMALE"
    case NEUTRAL => "NEUTRAL"
    case UNKNOWN => "UNKNOWN"
  }

  def animacyToByte(s:String) = s match {
    case "ANIMATE" => ANIMATE
    case "INANIMATE" => INANIMATE
    case "UNKNOWN" => UNKNOWN
  }
  
  def animacyFromByte(b:Byte) = b match {
    case ANIMATE => "ANIMATE"
    case INANIMATE => "INANIMATE"
    case UNKNOWN => "UNKNOWN"
  }
}


  //mentionSpan:String = "",
  //corefClusterID:Int = -1,
  //isRepresentative:Boolean = false,
  //isProper:Boolean = false,
