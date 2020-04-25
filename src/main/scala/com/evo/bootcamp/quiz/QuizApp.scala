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
import scala.util.Random

object QuizApp extends IOApp {
  private val token = "1168869271:AAH7ATc4umJxV054BdihWdqcdHsXeZFi50o"
  override implicit val contextShift = IO.contextShift(ExecutionContext.global)
  private val dbConfig = DbConfig("jdbc:postgresql://localhost:13423/postgres", "postgres", "password", "org.postgresql.Driver")

  override def run(args: List[String]): IO[ExitCode] =
    BlazeClientBuilder[IO](global).resource.use { client =>
      DaoInit.transactor[IO](dbConfig).use { db =>
        for {
          _ <- DaoInit.initialize(db)
          dao = new QuestionsDao[IO](db)
          logic = new TelegramBotLogic[IO](dao)
          api = new TelegramBotApi[IO](token, client, logic)
          _ <- (new TelegramBotProcess[IO](api, logic)).run
        } yield ExitCode.Success
      }
    }

//    BlazeClientBuilder[IO](global).resource.use { client =>
//      for {
//        api <- IO { new TelegramBotApi[IO](token, client) }
//        _ <- IO(new TelegramBotProcess[IO](api)).run().unsafeRunSync
//      } yield ()
//    }.as(ExitCode.Success)


}
