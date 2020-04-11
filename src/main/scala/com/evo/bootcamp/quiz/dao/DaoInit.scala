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
    val answersFr = Fragment.const(DaoCommon.answersSql)
    val questionsFr = Fragment.const(DaoCommon.questionsSql)
    val initDataFr = Fragment.const(DaoCommon.populateDataSql)
    (answersFr ++ questionsFr ++ initDataFr).update.run.transact(xa)
  }
}
