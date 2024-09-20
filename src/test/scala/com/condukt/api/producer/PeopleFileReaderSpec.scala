package com.condukt.api.producer

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class PeopleFileReaderSpec extends AnyWordSpec with Matchers {

  "PeopleFileReader.apply" should {
    "return people" when {
      "file exists and has correct json structure" in {
        val result = PeopleFileReader[IO]("test-random-people-data.json").unsafeRunSync()
        result.size mustBe 2
      }
    }
  }

}
