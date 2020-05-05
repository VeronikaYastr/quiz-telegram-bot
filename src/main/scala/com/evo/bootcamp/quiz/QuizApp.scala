package com.evo.bootcamp.quiz

import cats.effect._
import cats.effect.concurrent.Ref
import com.evo.bootcamp.quiz.config.DbConfig
import com.evo.bootcamp.quiz.dao.{DaoInit, QuestionsDao}
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.client.dsl.io._
import org.http4s.implicits._
import cats.syntax.apply._
import cats.implicits._
import com.evo.bootcamp.quiz.TelegramBotCommand.ChatId
import com.evo.bootcamp.quiz.dto.GameDto
import com.evo.bootcamp.quiz.dto.api.MessageResponse

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.global
import scala.concurrent.duration._

object QuizApp extends IOApp {
  private val token = "1168869271:AAH7ATc4umJxV054BdihWdqcdHsXeZFi50o"
  override implicit val contextShift = IO.contextShift(ExecutionContext.global)

  override def run(args: List[String]): IO[ExitCode] =
    DbConfig.load() flatMap { config =>
      BlazeClientBuilder[IO](global).resource.use { client =>
        DaoInit.transactor[IO](config).use { db =>
          for {
            _ <- DaoInit.initialize(db)
            dao = new QuestionsDao[IO](db)
            gameRef <- Ref[IO].of(Map.empty[ChatId, GameDto])
            fiberRef <- Ref[IO].of(Map.empty[ChatId, Fiber[IO, MessageResponse]])
            logic = new TelegramBotLogic[IO](dao, gameRef)
            api = new TelegramBotApi[IO](token, client)
            _ <- new TelegramBotProcess[IO](api, logic, fiberRef).run
          } yield ExitCode.Success
        }
      }
    }
}
