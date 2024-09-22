package com.condukt.api.convertors

import org.apache.kafka.common.KafkaFuture
import org.apache.kafka.common.KafkaFuture.BiConsumer

import scala.concurrent.{Future, Promise}

object KafkaFutureToScalaFuture {

  implicit def kafkaToScalaFuture[T](kafkaFuture: KafkaFuture[T]): Future[T] = {
    val promise = Promise[T]()
    kafkaFuture.whenComplete { new BiConsumer[T, Throwable] {
      override def accept(result: T, error: Throwable): Unit =
        if (error == null) {
          promise.success(result)
        } else {
          promise.failure(error)
        }
      }
    }
    promise.future
  }

}
