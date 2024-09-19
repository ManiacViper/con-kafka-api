package com.condukt.api

import cats.effect.Sync
import cats.implicits._
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl

object ApiRoutes {

  def kafkaRoutes[F[_]: Sync](H: PeopleQueryService[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F]{}
    import dsl._
    HttpRoutes.of[F] {
      case GET -> Root / "hello" / name =>
        for {
          greeting <- H.retrievePeople(PeopleQueryService.Name(name))
          resp <- Ok(greeting)
        } yield resp
    }
  }
}