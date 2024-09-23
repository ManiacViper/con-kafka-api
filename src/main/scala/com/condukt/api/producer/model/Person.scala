package com.condukt.api.producer.model

import org.http4s.EntityEncoder
import org.http4s.circe.jsonEncoderOf
import io.circe.generic.auto._

import java.time.LocalDate

// this is used everywhere right now, we could have seperate dto record case class for kafka and a case class for the rest api response
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
  implicit def greetingEntityEncoder[F[_]]: EntityEncoder[F, List[Person]] =
    jsonEncoderOf[F, List[Person]]
}
