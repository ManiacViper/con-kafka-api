package com.condukt.api

import cats.effect.{Async, Resource}
import com.comcast.ip4s._
import com.condukt.api.producer.{DefaultPeoplePopulatorService, PeopleFileReader, RandomPeopleProducerFactory}
import com.condukt.api.producer.model.Person
import fs2.io.net.Network
import org.apache.kafka.clients.producer.KafkaProducer
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits._
import org.http4s.server.middleware.Logger
import org.slf4j.LoggerFactory

object AppServer {

  private val logger = LoggerFactory.getLogger(this.getClass)

  //probably environment variables
  val kafkaBootstrapServers = "localhost:9092"
  val filePath = "random-people-data.json"
  val topic = "people"

  def run[F[_]: Async: Network]: F[Nothing] = {

    for {
      peopleProducer: KafkaProducer[String, Person] <- RandomPeopleProducerFactory(kafkaBootstrapServers)
      people <- Resource.eval(PeopleFileReader[F](filePath))
      _ = logger.info(s"read file, there are ${people.size} records")
      _ = new DefaultPeoplePopulatorService[F](peopleProducer).populateTopic(people, topic)
      _ = logger.info(s"records sent to topic: ${topic}")
      _ <- EmberClientBuilder.default[F].build
      peopleQueryService = PeopleQueryService.default[F]
      // Combine Service Routes into an HttpApp.
      // Can also be done via a Router if you
      // want to extract segments not checked
      // in the underlying routes.
      httpApp = (
        ApiRoutes.kafkaRoutes[F](peopleQueryService)
        ).orNotFound

      // With Middlewares in place
      finalHttpApp = Logger.httpApp(true, true)(httpApp)

      _ <- 
        EmberServerBuilder.default[F]
          .withHost(ipv4"0.0.0.0")
          .withPort(port"8080")
          .withHttpApp(finalHttpApp)
          .build
    } yield ()
  }.useForever
}
