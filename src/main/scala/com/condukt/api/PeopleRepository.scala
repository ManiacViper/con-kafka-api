package com.condukt.api

import cats.effect.Sync
import com.condukt.api.producer.model.Person
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.clients.consumer.KafkaConsumer

import java.time.Duration
import scala.annotation.tailrec
import scala.jdk.CollectionConverters._

trait PeopleRepository[F[_]] {
  def getPeople(topicName: String, offset: Long, count: Int): F[Seq[Person]]
}

class KafkaPeopleRepository[F[_]: Sync](kafkaConsumer: KafkaConsumer[String, Person]) extends PeopleRepository[F] {

  override def getPeople(topicName: String, offset: Long, count: Int): F[Seq[Person]] =
    Sync[F].delay {
      val allPartitions =
        kafkaConsumer
          .partitionsFor(topicName)
          .asScala
          .toList
          .map(partitionInfo => new TopicPartition(topicName, partitionInfo.partition()))

      kafkaConsumer.assign(allPartitions.asJava)

      allPartitions
        .foreach { partitionInfo =>
          val topicPartition = new TopicPartition(topicName, partitionInfo.partition())
          kafkaConsumer.seek(topicPartition, offset)
        }
      handleRetrieval(Seq.empty, offset, count)
    }

  @tailrec
  private def handleRetrieval(recordsSoFar: Seq[Person], offset: Long, count: Int): Seq[Person] = {
    val currentRecordsPulled =
      kafkaConsumer
        .poll(Duration.ofMillis(500))
        .asScala
        .tapEach(record => println(s"id=${record.value()._id},offset=${record.offset()}"))
        .filter(record => record.offset() >= offset)
        .map(_.value())
        .toSeq
    val total = (currentRecordsPulled ++ recordsSoFar).take(count)

    println(s"${total.map(_._id)}")

    if(total.size == count || currentRecordsPulled.isEmpty) {
      total
    } else {
      handleRetrieval(total, offset, count)
    }
  }
}