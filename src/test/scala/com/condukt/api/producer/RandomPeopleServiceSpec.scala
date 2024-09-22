package com.condukt.api.producer

import cats.effect.IO
import cats.effect.kernel.Resource
import cats.effect.unsafe.implicits.global
import com.condukt.api.producer.RandomPeopleServiceSpec.{readMessages, withKafka}
import com.condukt.api.producer.model.{Person, PersonDeserializer}
import com.condukt.api.producer.model.PersonSpec.defaultPerson
import com.dimafeng.testcontainers.KafkaContainer
import org.apache.kafka.clients.admin.{AdminClient, NewTopic}
import org.apache.kafka.clients.consumer.{ConsumerRecords, KafkaConsumer}
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.common.serialization.StringDeserializer
import org.scalatest.Assertion
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.slf4j.{Logger, LoggerFactory}
import org.testcontainers.utility.DockerImageName

import java.util.{Properties, UUID}
import scala.jdk.CollectionConverters._

//would reduce the time taken for tests, stopping container only after all the tests are run
class RandomPeopleServiceSpec extends AnyWordSpec with Matchers {

  val container: KafkaContainer = KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.7.0"))
  val logger: Logger = LoggerFactory.getLogger(this.getClass)

  "RandomPeopleService.populateTopic" should {
    "populate with list of people" when {
      "the list is provided" in {
        val topicName = s"people-${UUID.randomUUID()}"
        val secondPerson = defaultPerson.copy(_id = "168YCC2THQH1HEAB")
        val records = List(defaultPerson, secondPerson)

        withKafka(container, topicName) { producer =>
          new DefaultRandomPeopleService[IO](producer).populateTopic(records, topicName).unsafeRunSync()
          val noOfMessages = readMessages(container.bootstrapServers, topicName).unsafeRunSync()

          noOfMessages must contain theSameElementsAs records
        }
      }
    }

    "does not produce records of people" when {
      "list is empty" in {
        val topicName = s"people-${UUID.randomUUID()}"
        val emptyRecords = List.empty

        withKafka(container, topicName) { producer =>
          new DefaultRandomPeopleService[IO](producer).populateTopic(emptyRecords, topicName).unsafeRunSync()
          val noOfMessages = readMessages(container.bootstrapServers, topicName).unsafeRunSync().size
          noOfMessages mustBe 0
        }
      }
    }

    //another test could be produce no record for an error here but it was tricky to induce an error, could make it happen with a mocking library
  }

}

object RandomPeopleServiceSpec {

  def createKafkaClient(boostrapServers: String): Resource[IO, AdminClient] =
    Resource.make {
      IO {
        val props = new Properties()
        props.put("bootstrap.servers", boostrapServers)
        val adminClient: AdminClient = AdminClient.create(props)
        adminClient
      }
    }(client => IO(client.close()))

  private def createTopic(adminClient: AdminClient, topicName: String, partitions: Int = 3, replicationFactor: Short = 1): IO[Unit] = IO.blocking {
      val newTopic = new NewTopic(topicName, partitions, replicationFactor)
      val _ = adminClient.createTopics(java.util.Collections.singleton(newTopic)).all().get()
  }

  def withKafka(container: KafkaContainer, topic: String)
               (testFn: KafkaProducer[String, Person] => Assertion): Assertion = {
    IO(container.start())
      .flatMap { _ =>
        createKafkaClient(container.bootstrapServers)
          .use { client =>
            for {
              _                 <- createTopic(client, topic)
              producerResource  = RandomPeopleProducerFactory[IO](container.bootstrapServers)
              result            <- producerResource.use(producer => IO(testFn(producer)))
              _                 <- IO(container.stop())
            } yield result
          }
      }.unsafeRunSync()
  }

  def readMessages(bootstrapServers: String, topic: String): IO[List[Person]] = {
      IO.blocking {
          val props = new Properties()
          props.put("bootstrap.servers", bootstrapServers)
          props.put("group.id", "test-group")
          props.put("key.deserializer", classOf[StringDeserializer].getName)
          props.put("value.deserializer", classOf[PersonDeserializer].getName)
          props.put("enable.auto.commit", "true")
          props.put("auto.offset.reset", "earliest")

          val consumer = new KafkaConsumer[String, Person](props)
          consumer.subscribe(List(topic).asJava)

          val records: ConsumerRecords[String, Person] = consumer.poll(java.time.Duration.ofMillis(1000))
          val messages: List[Person] = records.asScala.map(record => record.value()).toList

          consumer.close()
          messages
      }
  }


}
