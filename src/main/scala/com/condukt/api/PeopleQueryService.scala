package com.condukt.api

import cats.effect.Sync
import com.condukt.api.producer.model.{Address, Person}

import java.time.LocalDate
import java.time.format.DateTimeFormatter

trait PeopleQueryService[F[_]]{
  def retrievePeople(topic: String, offset: Int, count: Int): F[List[Person]]
}

object PeopleQueryService {

  private val mockPerson: Person = Person(_id = "368YCC2THQH1HEAQ", name = "Kiana Yoo",
    dob = LocalDate.parse("2021-05-31", DateTimeFormatter.ISO_LOCAL_DATE),
    address = Address("2745 Shaftsbury Circle", "Slough", "LS67 1ID"),
    telephone = "+39-3380-033-155", pets = List("Sadie", "Rosie"),
    score = 3.7, email = "palma14@hotmail.com", url = "http://earth.com",
    description = "strips rt administrators composer ...", verified = true, salary = 31900)

  val mockPeople = List(mockPerson, mockPerson.copy(_id = "111YCC2THQH1QAEH"))

  def default[F[_]: Sync]: PeopleQueryService[F] = new PeopleQueryService[F] {
    def retrievePeople(topic: String, offset: Int, count: Int): F[List[Person]] =
      Sync[F].delay(mockPeople)
  }
}
