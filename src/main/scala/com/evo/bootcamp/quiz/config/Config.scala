package com.evo.bootcamp.quiz.config

import cats.effect.IO
import io.circe.config.parser
import io.circe.generic.auto._

case class ServerConfig(host: String, port: Int)

case class DbConfig(url: String, username: String, password: String, driverName: String)

case class Config(serverConfig: ServerConfig, dbConfig: DbConfig)

object Config {
  def load(): IO[Config] = {
    for {
      dbConf <- parser.decodePathF[IO, DbConfig]("db")
      serverConf <- parser.decodePathF[IO, ServerConfig]("server")
    } yield Config(serverConf, dbConf)
  }
}
