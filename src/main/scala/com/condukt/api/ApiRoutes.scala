package com.condukt.api

import cats.effect.Sync
import cats.implicits._
import org.http4s.{HttpRoutes, Response}
import org.http4s.dsl.Http4sDsl
import org.slf4j.LoggerFactory

object ApiRoutes {

  private val logger = LoggerFactory.getLogger(this.getClass)

  def kafkaRoutes[F[_]: Sync](queryService: PeopleQueryService[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F]{}
    import dsl._
    object Count extends QueryParamDecoderMatcherWithDefault[Int]("count", 10)

    def getPeople(topicName: String, offset: Long, count: Int): F[Response[F]] = {
      println(s"[offset=$offset, count=$count]")
      logger.info(s"[offset=$offset, count=$count]")
      for {
        people <- queryService.retrievePeople(topicName, offset, count)
        resp <- Ok(people)
      } yield resp
    }

    //havent done any bad request validation, returns 404 when string is passed for either or both path and query param
    HttpRoutes.of[F] {
      case GET -> Root / "topic" / topic_name / LongVar(offset) :? Count(count: Int) =>
        getPeople(topic_name, offset, count)
      case GET -> Root / "topic" / topic_name :? Count(count: Int) =>
        val offset: Long = 0
        getPeople(topic_name, offset, count)
    }
  }

}