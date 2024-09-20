package com.condukt.api.producer.model

import java.time.LocalDate

final case class Person(
                         _id: String,
                         name: String,
                         dob: LocalDate,
                         address: Address,
                         telephone: String,
                         pets: List[String],
                         score: Double,
                         email: String,
                         url: String,
                         description: String,
                         verified: Boolean,
                         salary: Int
                 )
