package com.evo.bootcamp.quiz.dao

import cats.effect.{ContextShift, IO}
import com.evo.bootcamp.quiz.config.DbConfig
import doobie.Fragment
import doobie.util.transactor.Transactor
import doobie.implicits._
import doobie.h2._

object DaoInit {
  def transactor(dbConfig: DbConfig)(implicit cs: ContextShift[IO]): Transactor[IO] = {
      Transactor.fromDriverManager[IO](
        url = dbConfig.url,
        user = dbConfig.username,
        pass = dbConfig.password,
        driver = dbConfig.driverName
      )
  }

  def initTables(xa: Transactor[IO]): IO[Int] = {
    val authorsFr = Fragment.const(DaoCommon.authorsSql)
    val booksFr = Fragment.const(DaoCommon.booksSql)
    val initDataFr = Fragment.const(DaoCommon.populateDataSql)
    (authorsFr ++ booksFr ++ initDataFr).update.run.transact(xa)
  }
}
