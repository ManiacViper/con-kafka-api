package com.condukt.api

import cats.Applicative
import cats.implicits._
import io.circe.{Encoder, Json}
import org.http4s.EntityEncoder
import org.http4s.circe._

trait PeopleQueryService[F[_]]{
  def retrievePeople(n: PeopleQueryService.Name): F[PeopleQueryService.Greeting]
}

object PeopleQueryService {
  final case class Name(name: String) extends AnyVal
  final case class Greeting(greeting: String) extends AnyVal
  object Greeting {
    implicit val greetingEncoder: Encoder[Greeting] = new Encoder[Greeting] {
      final def apply(a: Greeting): Json = Json.obj(
        ("message", Json.fromString(a.greeting)),
      )
    }
    implicit def greetingEntityEncoder[F[_]]: EntityEncoder[F, Greeting] =
      jsonEncoderOf[F, Greeting]
  }

  def impl[F[_]: Applicative]: PeopleQueryService[F] = new PeopleQueryService[F]{
    def retrievePeople(n: PeopleQueryService.Name): F[PeopleQueryService.Greeting] =
        Greeting("Hello, " + n.name).pure[F]
  }
}
