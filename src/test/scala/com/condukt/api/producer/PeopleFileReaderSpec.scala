package com.condukt.api.producer

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import io.circe.DecodingFailure
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.nio.file.NoSuchFileException

class PeopleFileReaderSpec extends AnyWordSpec with Matchers {

  "PeopleFileReader.apply" should {
    "return people" when {
      "file exists and has correct json structure" in {
        val result = PeopleFileReader[IO]("test-random-people-data.json").unsafeRunSync()
        result.size mustBe 2
      }
    }

    "return error" when {
      "no file exists" in {
        val error = intercept[NoSuchFileException] {
          PeopleFileReader[IO]("invalid-file.json").unsafeRunSync()
        }
        error.getMessage mustBe s"invalid-file.json not found"
      }
      "file exists but is not the correct json structure" in {
        val error = intercept[DecodingFailure] {
          PeopleFileReader[IO]("invalid-random-people-data.json").unsafeRunSync()
        }
        error.getMessage mustBe s"DecodingFailure at .ctRoot[0].address.postode: Missing required field"
      }
    }
  }

}
