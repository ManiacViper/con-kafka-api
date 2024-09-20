package com.condukt.api.producer

import cats.effect.{Resource, Sync}
import cats.syntax.flatMap._
import cats.syntax.functor._
import com.condukt.api.producer.model.{Person, RandomPeople}
import io.circe.generic.auto._
import io.circe.parser.parse
import io.circe.{DecodingFailure, Json}

import scala.io.Source

object PeopleFileReader {
  def apply[F[_] : Sync](filePath: String): F[List[Person]] = {
    val resourceStream = getClass.getClassLoader.getResourceAsStream(filePath)
    val fileContent = Resource.make(Sync[F].delay(Source.fromInputStream(resourceStream)))(source => Sync[F].delay(source.close()))
    val json: F[Json] = fileContent.use(source => Sync[F].delay(parse(source.getLines().mkString).getOrElse(Json.Null)))
    val decodePeople: F[Either[DecodingFailure, List[Person]]] = json.map(_.as[RandomPeople].map(_.ctRoot))
    decodePeople.flatMap {
      case Right(people) =>
        Sync[F].pure(people)
      case Left(error) =>
        Sync[F].raiseError(error)
    }
  }
}
