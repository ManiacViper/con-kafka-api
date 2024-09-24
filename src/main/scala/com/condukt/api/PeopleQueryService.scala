package com.condukt.api

import com.condukt.api.consumer.PeopleRepository
import com.condukt.api.producer.model.Person

trait PeopleQueryService[F[_]]{
  def retrievePeople(topic: String, offset: Long, count: Int): F[Seq[Person]]
}

object PeopleQueryService {

  def default[F[_]](peopleRepository: PeopleRepository[F]): PeopleQueryService[F] = new PeopleQueryService[F] {
    def retrievePeople(topic: String, offset: Long, count: Int): F[Seq[Person]] =
      peopleRepository.getPeople(topic, offset, count)
  }
}
