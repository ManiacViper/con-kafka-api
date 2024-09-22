package com.condukt.api.convertors

import com.condukt.api.convertors.KafkaFutureToScalaFuture._
import org.apache.kafka.common.internals.KafkaFutureImpl
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.concurrent.ScalaFutures

class KafkaFutureToScalaFutureSpec extends AnyWordSpec with Matchers {

  "KafkaFutureToScalaFuture.kafkaToScalaFuture" should {
    "return value" when {
      "kafka future is successful" in {
        val kafkaFuture = new KafkaFutureImpl[Int]()
        kafkaFuture.complete(1)

        val result = kafkaFuture

        ScalaFutures.convertScalaFuture(result).futureValue mustBe 1
      }
    }

    "return error" when {
      "kafka future has failed" in {
        val kafkaFuture = new KafkaFutureImpl[Int]()
        val expectedError = new RuntimeException("some exception")
        kafkaFuture.completeExceptionally(expectedError)

        val result = intercept[RuntimeException](ScalaFutures.convertScalaFuture(kafkaFuture).futureValue)

        result.getMessage mustBe s"The future returned an exception of type: java.lang.RuntimeException, with message: ${expectedError.getMessage}."
      }
    }


  }
}
