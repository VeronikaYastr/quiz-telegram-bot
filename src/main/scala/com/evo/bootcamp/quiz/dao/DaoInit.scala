package com.evo.bootcamp.quiz.dao

import cats.effect.{ContextShift, Effect, IO}
import com.evo.bootcamp.quiz.config.DbConfig
import doobie.Fragment
import doobie.util.transactor.Transactor
import doobie.implicits._
import doobie.postgres._

class DaoInit[F[_]](implicit F: Effect[F]) {
  def transactor(dbConfig: DbConfig)(implicit cs: ContextShift[F]): Transactor[F] = {
      Transactor.fromDriverManager[F](
        url = dbConfig.url,
        user = dbConfig.username,
        pass = dbConfig.password,
        driver = dbConfig.driverName
      )
  }
}
