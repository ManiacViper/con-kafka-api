package com.condukt.api

import cats.effect.{Async, Resource}
import com.comcast.ip4s._
import com.condukt.api.consumer.{KafkaPeopleRepository, PersonConsumer}
import com.condukt.api.producer.{DefaultPeoplePopulatorService, PeopleFileReader, PersonProducer}
import fs2.io.net.Network
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
  val consumerGroupId = "person-consumer-group"
  val pollMaxRecords = 50

  def run[F[_]: Async: Network]: F[Nothing] = {

    for {
      personProducer <- PersonProducer(kafkaBootstrapServers)
      personConsumer <- PersonConsumer(kafkaBootstrapServers, consumerGroupId, pollMaxRecords)
      people <- Resource.eval(PeopleFileReader[F](filePath))
      _ = logger.info(s"read file, there are ${people.size} records")
      _ = new DefaultPeoplePopulatorService[F](personProducer).populateTopic(people, topic)
      _ = logger.info(s"records sent to topic: ${topic}")
      _ <- EmberClientBuilder.default[F].build
      repository = new KafkaPeopleRepository(personConsumer)
      peopleQueryService = PeopleQueryService.default[F](repository)
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
