package com.condukt.api.producer.model

import org.apache.kafka.common.serialization.{Deserializer, Serializer}
import io.circe.syntax._
import io.circe.parser._
import io.circe.generic.auto._

class PersonSerializer extends Serializer[Person] {
  override def serialize(topic: String, data: Person): Array[Byte] = {
    if (data == null) null
    else data.asJson.noSpaces.getBytes("UTF-8")
  }
}

class PersonDeserializer extends Deserializer[Person] {
  override def deserialize(topic: String, data: Array[Byte]): Person = {
    if (data == null) null
    else {
      val jsonString = new String(data, "UTF-8")
      parse(jsonString).flatMap(_.as[Person]).getOrElse(null)
    }
  }
}

