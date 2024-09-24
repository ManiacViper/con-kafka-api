package com.condukt.api.consumer

import cats.effect.{Resource, Sync}
import com.condukt.api.producer.model.{Person, PersonDeserializer}
import org.apache.kafka.clients.consumer.{ConsumerConfig, KafkaConsumer}
import org.apache.kafka.common.serialization.StringDeserializer

import java.util.Properties

object PersonConsumer {

  def apply[F[_]: Sync](bootstrapServers: String, groupId: String, maxPollRecords: Int): Resource[F, KafkaConsumer[String, Person]] =
    Resource.make {
      Sync[F].delay {
        val props = new Properties()
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId)
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, classOf[StringDeserializer].getName)
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, classOf[PersonDeserializer].getName)
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false")
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollRecords)

        new KafkaConsumer[String, Person](props)
      }
    }(consumer => Sync[F].delay(consumer.close()))

}
