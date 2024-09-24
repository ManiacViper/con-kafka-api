package com.condukt.api

import cats.effect.Sync
import com.condukt.api.producer.model.Person
import org.apache.kafka.clients.admin.{AdminClient, TopicDescription}
import org.apache.kafka.common.{KafkaFuture, TopicPartition}
import org.apache.kafka.clients.consumer.KafkaConsumer

import java.time.Duration
import scala.annotation.tailrec
import scala.jdk.CollectionConverters._
import scala.util.{Failure, Success, Try}

trait PeopleRepository[F[_]] {
  def getPeople(topicName: String, offset: Long, count: Int): F[Seq[Person]]
}

class KafkaPeopleRepository[F[_]: Sync](adminClient: AdminClient, kafkaConsumer: KafkaConsumer[String, Person]) extends PeopleRepository[F] {


  override def getPeople(topicName: String, offset: Long, count: Int): F[Seq[Person]] =
    Sync[F].blocking {
      val topicDescription: KafkaFuture[TopicDescription] =
        adminClient
          .describeTopics(List(topicName).asJava)
          .topicNameValues()
          .get(topicName)
      getPeopleFromTopic(topicName, offset, count, topicDescription.get())
    }


  private def getPeopleFromTopic(topicName: String, offset: Long, count: Int, topicDescription: TopicDescription): Seq[Person] = {
    topicDescription
      .partitions()
      .asScala
      .flatMap { partitionInfo =>
        val partition = new TopicPartition(topicName, partitionInfo.partition())
        val maybePeople =
          Try(kafkaConsumer.assign(List(partition).asJava))
            .flatMap { _ =>
              Try(kafkaConsumer.seek(partition, offset))
            }
        maybePeople match {
          case Success(_) =>
            getRecords(List.empty, count)
              .take(count)
          case Failure(exception) =>
            println(s"${exception.getMessage}")
            Seq.empty
        }
    }.toList
  }

  @tailrec
  private def getRecords(people: List[Person], count: Int): List[Person] = {
    if(people.size >= count) {
      people
    } else {
      val morePeople =
        kafkaConsumer
          .poll(Duration.ofMillis(500))
          .asScala
          .map(_.value())
          .toList
      if(morePeople.isEmpty) {
        people
      } else {
        getRecords(people ++ morePeople, count)
      }
    }
  }
}