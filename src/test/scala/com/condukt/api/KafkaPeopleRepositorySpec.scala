package com.condukt.api

import cats.effect.{IO, Resource}
import cats.effect.unsafe.implicits.global
import com.condukt.api.KafkaPeopleRepositorySpec._
import com.condukt.api.producer.model.{Person, PersonDeserializer, PersonSerializer, PersonSpec}
import com.dimafeng.testcontainers.KafkaContainer
import org.apache.kafka.clients.admin.{AdminClient, AdminClientConfig, NewTopic}
import org.apache.kafka.clients.consumer.{ConsumerConfig, KafkaConsumer}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}
import org.scalatest.Assertion
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.slf4j.{Logger, LoggerFactory}
import org.testcontainers.utility.DockerImageName

import java.util.Properties

class KafkaPeopleRepositorySpec extends AnyWordSpec with Matchers {

  "KafkaPeopleRepository.getPeople" should {
    "return people" when {
      "they exist as records in the topic" in {
        val secondPerson = PersonSpec.defaultPerson.copy("2222222222222222")
        val thirdPerson = PersonSpec.defaultPerson.copy("333333333333333")
        val people = List(PersonSpec.defaultPerson, secondPerson, thirdPerson)
        withKafka(people) { (adminClient, consumer) =>
          val repository = new KafkaPeopleRepository[IO](adminClient, consumer)
          val result = repository.getPeople("people", 1, 2).unsafeRunSync()
          result mustBe List(secondPerson, thirdPerson)
        }
      }
    }
  }
}

object KafkaPeopleRepositorySpec {

  val logger: Logger = LoggerFactory.getLogger(this.getClass)

  def withKafka(people: List[Person])(testFn: (AdminClient, KafkaConsumer[String, Person]) => Assertion): Unit =
    containerResource()
      .flatMap { container =>
        for {
          _ <- Resource.eval(IO(container.start()))
          _ = println("container started")
          _ = logger.info("container started")
          producer <- testProducer(container.bootstrapServers)
          client <- testAdminClientResource(container.bootstrapServers)
          topic = "people"
          _ <- Resource.eval(createTestTopic(client, topic))
          _ <- Resource.eval(populateTopic(topic, people, producer))
          consumer <- testConsumer(container.bootstrapServers)
          _ <- Resource.eval(IO(testFn(client, consumer)))
        } yield ()
      }
      .use { _ =>
        IO.unit
      }.unsafeRunSync()


  private def populateTopic(topic: String, people: List[Person], producer: KafkaProducer[String, Person]) = IO.blocking {
    people.foreach { person =>
      val record = new ProducerRecord[String, Person](topic, person._id, person)
      producer.send(record).get()
    }
  }

  private def containerResource(): Resource[IO, KafkaContainer] =
    Resource.make(IO(KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.7.0"))))(container => IO(container.stop()))

  private def createTestTopic(adminClient: AdminClient, topicName: String, partitions: Int = 3, replicationFactor: Short = 1): IO[Unit] = IO.blocking {
    val newTopic = new NewTopic(topicName, partitions, replicationFactor)
    val _ = adminClient.createTopics(java.util.Collections.singleton(newTopic)).all().get()
  }

  private def testAdminClientResource(bootstrapServers: String): Resource[IO, AdminClient] = Resource.make {
    IO {
      val props = new Properties()
      props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
      props.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, "500")

      AdminClient.create(props)
    }
  }(client => IO(client.close()))

  private def testConsumer(bootstrapServers: String): Resource[IO, KafkaConsumer[String, Person]] = Resource.make {
    IO {
      val props = new Properties()
      props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
      props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-repository-consumer-group")
      props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, classOf[StringDeserializer].getName)
      props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, classOf[PersonDeserializer].getName)
      props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false")
      props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
      props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 1)

      new KafkaConsumer[String, Person](props)
    }
  }(consumer => IO(consumer.close()))

  private def testProducer(bootstrapServers: String):  Resource[IO, KafkaProducer[String, Person]] = {
    val props = new Properties()
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer].getName)
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, classOf[PersonSerializer].getName)

    Resource.make(IO(new KafkaProducer[String, Person](props)))(producer => IO(producer.close()))
  }

}
