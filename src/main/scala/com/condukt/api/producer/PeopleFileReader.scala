package com.condukt.api.producer

import cats.effect.{Resource, Sync}
import cats.syntax.flatMap._
import cats.syntax.functor._
import com.condukt.api.producer.model.{Person, RandomPeople}
import io.circe.generic.auto._
import io.circe.parser.parse
import io.circe.{DecodingFailure, Json}
import java.nio.file.NoSuchFileException
import scala.io.Source

object PeopleFileReader {
  /*
    if this was part of the production app where it populated the topic before starting, i would do the below,
    this takes the whole list in memory, would be looking to send each person record decoded and then load the next person record
    this way we reduce memory usage by the app significantly

    Would need to accumulate lines until we hit comma most likely to indicate a complete person record
    also the final person record might need special handling to say if it has } and then ] that's a complete person record  
   */
  def apply[F[_] : Sync](filePath: String): F[List[Person]] = {
    val maybeResourceStream = Option(getClass.getClassLoader.getResourceAsStream(filePath))
    val fileContent = maybeResourceStream match {
      case Some(resourceStream) =>
        Resource.make(Sync[F].delay(Source.fromInputStream(resourceStream)))(source => Sync[F].delay(source.close()))
      case None =>
        Resource.eval(Sync[F].raiseError(new NoSuchFileException(s"$filePath not found")))
    }
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
