package com.condukt.api

import cats.effect.IO
import com.condukt.api.ApiRouteSpec.mockPeopleJsonString
import com.condukt.api.consumer.PeopleRepository
import com.condukt.api.producer.model.{Person, PersonSpec}
import io.circe.Json
import io.circe.parser.parse
import org.http4s._
import org.http4s.implicits._
import munit.CatsEffectSuite

class ApiRouteSpec extends CatsEffectSuite {

  test("topic returns status code 200") {
    assertIO(returnPeople.map(_.status) ,Status.Ok)
  }

  test("topic returns people") {
    val Right(expected) = parse(mockPeopleJsonString)
    def actual(value: String) = parse(value).getOrElse(Json.Null)

    assertIO(returnPeople.flatMap(_.as[String].map(actual)), expected)
  }

  private[this] val returnPeople: IO[Response[IO]] = {
    val getPeople = Request[IO](Method.GET, uri"/topic/people")
    val mockPeopleResult = List(PersonSpec.defaultPerson, PersonSpec.defaultPerson.copy(_id = "111YCC2THQH1QAEH"))
    val repositoryStub = new PeopleRepository[IO]{
      override def getPeople(topicName: String, offset: Long, count: Int): IO[Seq[Person]] =
        IO(mockPeopleResult)
    }
    val service = PeopleQueryService.default[IO](peopleRepository = repositoryStub)
    ApiRoutes.kafkaRoutes(service).orNotFound(getPeople)
  }
}

object ApiRouteSpec {
  val mockPeopleJsonString: String = """[
                                          |{
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
                                          | },
                                          | {
                                          | "_id": "111YCC2THQH1QAEH",
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
                                          | }
                                          | ]""".stripMargin
}