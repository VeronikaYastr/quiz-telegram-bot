package com.evo.bootcamp.quiz

import org.http4s._
import org.http4s.client.dsl.io._
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.implicits._
import cats.syntax.apply._
import cats.implicits._

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.global
import cats.effect._
import com.evo.bootcamp.quiz.config.DbConfig
import com.evo.bootcamp.quiz.dao.{DaoInit, QuestionsDao}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

object QuizApp extends IOApp {
  private val token = "1168869271:AAH7ATc4umJxV054BdihWdqcdHsXeZFi50o"
  override implicit val contextShift = IO.contextShift(ExecutionContext.global)
  override implicit val timer = IO.timer(ExecutionContext.global)

  override def run(args: List[String]): IO[ExitCode] =
    BlazeClientBuilder[IO](global).resource.use { client =>
      for {
        init <- IO {
          new DaoInit[IO]()
        }
        tr <- IO {
          init.transactor(DbConfig("jdbc:postgresql://localhost:5432/postgres", "postgres", "password", "org.postgresql.Driver"))
        }
        dao <- IO {
          new QuestionsDao[IO](tr)
        }
        logic <- IO {
          new TelegramBotLogic[IO](dao)
        }
        api <- IO {
          new TelegramBotApi[IO](token, client, logic)
        }
        _ <- (new TelegramBotProcess[IO](api)).run

      } yield ExitCode.Success
    }
//    BlazeClientBuilder[IO](global).resource.use { client =>
//      for {
//        api <- IO { new TelegramBotApi[IO](token, client) }
//        _ <- IO(new TelegramBotProcess[IO](api)).run().unsafeRunSync
//      } yield ()
//    }.as(ExitCode.Success)


}
