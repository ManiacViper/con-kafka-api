package com.condukt.api.producer.model

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto._

import java.time.LocalDate

final case class Person(
                         _id: String,
                         name: String,
                         dob: LocalDate,
                         address: Address,
                         telephone: String,
                         pets: List[String],
                         score: Double,
                         email: String,
                         url: String,
                         description: String,
                         verified: Boolean,
                         salary: Int
                 )

object Person {

  implicit val addressDecoder: Decoder[Address] = deriveDecoder
  implicit val addressEncoder: Encoder[Address] = deriveEncoder

  implicit val personDecoder: Decoder[Person] = deriveDecoder
  implicit val personEncoder: Encoder[Person] = deriveEncoder
}
