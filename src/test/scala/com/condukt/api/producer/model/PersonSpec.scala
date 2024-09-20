package com.condukt.api.producer.model

import com.condukt.api.producer.model.PersonSpec.{defaultPerson, defaultPersonJsonString}
import io.circe.syntax.EncoderOps
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import io.circe.parser._
import io.circe.generic.auto._

class PersonSpec extends AnyWordSpec with Matchers {

  "Person" should {
    "return correct json" when {
      "serializing" in {
        val result = defaultPerson.asJson

        val Right(expected) = parse(defaultPersonJsonString)
        result mustBe expected
      }
    }

    "return correct case class" when {
      "deserializing" in {
        val Right(result) = parse(defaultPersonJsonString).flatMap(_.as[Person])
        result mustBe defaultPerson
      }
    }
  }

}

object PersonSpec {
  val defaultPerson: Person =
    Person(_id = "368YCC2THQH1HEAQ", name = "Kiana Yoo",
          dob = LocalDate.parse("2021-05-31", DateTimeFormatter.ISO_LOCAL_DATE),
          address = Address("2745 Shaftsbury Circle", "Slough", "LS67 1ID"),
          telephone = "+39-3380-033-155", pets = List("Sadie", "Rosie"),
          score = 3.7, email = "palma14@hotmail.com", url = "http://earth.com",
          description = "strips rt administrators composer ...", verified = true, salary = 31900)
  val defaultPersonJsonString: String = """{
                                  | "_id": "368YCC2THQH1HEAQ",
                                  | "name": "Kiana Yoo",
                                  | "dob": "2021-05-31",
                                  | "address": {
                                  | "street": "2745 Shaftsbury Circle",
                                  | "town": "Slough",
                                  | "postode": "LS67 1ID"
                                  | },
                                  | "telephone": "+39-3380-033-155",
                                  | "pets": [
                                  | "Sadie",
                                  | "Rosie"
                                  | ],
                                  | "score": 3.7,
                                  | "email": "palma14@hotmail.com",
                                  | "url": "http://earth.com",
                                  | "description": "strips rt administrators composer ...",
                                  | "verified": true,
                                  | "salary": 31900
                                  | }""".stripMargin

}
