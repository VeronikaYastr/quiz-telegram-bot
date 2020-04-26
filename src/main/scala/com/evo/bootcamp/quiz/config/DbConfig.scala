package com.evo.bootcamp.quiz.config

import cats.effect.IO
import io.circe.config.parser
import io.circe.generic.auto._

case class DbConfig(url: String, username: String, password: String, driverName: String)

object DbConfig {
  def load(): IO[DbConfig] = {
    parser.decodePathF[IO, DbConfig]("db")
  }
}
