package com.condukt.api.producer

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}

import java.util.Properties
import cats.effect.{Resource, Sync}
import com.condukt.api.producer.model.{Person, PersonSerializer}
import org.apache.kafka.common.serialization.StringSerializer
import cats.syntax.all._

trait PeoplePopulatorService[F[_]] {
  def populateTopic(topic: String, filePath: String): F[Unit]
}

class DefaultPeoplePopulatorService[F[_]: Sync](producer: KafkaProducer[String, Person]) {

  def populateTopic(people: List[Person], topic: String): F[Unit] = {
    people
      .map { person =>
        val record = new ProducerRecord[String, Person](topic, person._id, person)
        Sync[F]
          .delay {
            producer.send(record)
          }
          .void
          .handleErrorWith { ex =>
            Sync[F].raiseError(new RuntimeException(s"Failed to send record for person ${person._id}", ex))
          }
      }
      .sequence
      .flatMap { _ =>
        Sync[F]
          .delay(producer.flush())
      }
  }

}

object PersonProducer {
  def apply[F[_]: Sync](broker: String): Resource[F, KafkaProducer[String, Person]] = {
    val props = new Properties()
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, broker)
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer].getName)
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, classOf[PersonSerializer].getName)
    props.put(ProducerConfig.ACKS_CONFIG, "all")

    Resource.make(Sync[F].delay(new KafkaProducer[String, Person](props)))(producer => Sync[F].delay(producer.close()))
  }
}
