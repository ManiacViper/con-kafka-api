package com.condukt.api

import cats.effect.{IO, IOApp}

object Main extends IOApp.Simple {
  val run = AppServer.run[IO]
}
