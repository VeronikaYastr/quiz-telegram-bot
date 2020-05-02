package com.evo.bootcamp.quiz

import cats.effect.{IO, _}
import cats.effect.concurrent.Ref
import com.evo.bootcamp.quiz.TelegramBotCommand.{ChatId, _}
import com.evo.bootcamp.quiz.config.DbConfig
import com.evo.bootcamp.quiz.dao.{DaoInit, QuestionsDao}
import com.evo.bootcamp.quiz.dto.GameDto
import com.evo.bootcamp.quiz.dto.api.CallbackQuery.User
import com.evo.bootcamp.quiz.dto.api.MessageResponse.Result
import com.evo.bootcamp.quiz.dto.api._
import com.evo.bootcamp.quiz.utils.MessageTexts._
import org.mockito.MockitoSugar
import org.scalatest._

import scala.concurrent.ExecutionContext

class LogicTest extends FlatSpec with BeforeAndAfter with MockitoSugar {
  implicit val contextShift = IO.contextShift(ExecutionContext.global)
  implicit val timer: Timer[IO] = IO.timer(ExecutionContext.global)
  val mockApi = mock[TelegramBotApi[IO]]

  val messageHandler = for {
    config <- DbConfig.load()
    handler <- DaoInit.transactor[IO](config).use { db =>
      for {
        _ <- DaoInit.initialize(db)
        dao = new QuestionsDao[IO](db)
        gameRef <- Ref[IO].of(Map.empty[ChatId, GameDto])
        fiberRef <- Ref[IO].of(Map.empty[ChatId, Fiber[IO, MessageResponse]])
        logic = new TelegramBotLogic[IO](dao, gameRef)
        process = new TelegramBotProcess[IO](mockApi, logic, fiberRef)
        handler <- mockApi.requestUpdates(0)
          .flatMap(response => process.processMessage(response).map(_.getOrElse(0L)))
      } yield handler
    }
  } yield handler

  it should "process help message" in {
    val chatId = 1L
    when(mockApi.requestUpdates(0)).thenReturn(
      IO { BotResponse(ok = true, List(BotUpdateMessage(0L, Some(Message(0L, Chat(chatId), Some(`help`))), None))) }
    )
    when(mockApi.sendMessage(chatId, `helpMessage`)).thenReturn(
      IO { MessageResponse(ok = true, Result(0L)) }
    )

    messageHandler.unsafeRunSync

    verify(mockApi).sendMessage(chatId, `helpMessage`)
  }

  it should "process start game message" in {
    val chatId = 2L
    val buttons = List(
      List(InlineKeyboardButton("5", s"5 $chatId"), InlineKeyboardButton("10", s"10 $chatId")),
      List(InlineKeyboardButton("15", s"15 $chatId"), InlineKeyboardButton("20", s"20 $chatId"))
    )

    when(mockApi.requestUpdates(0)).thenReturn(
      IO { BotResponse(ok = true, List(BotUpdateMessage(0L, Some(Message(2L, Chat(chatId), Some(`start`))), None))) }
    )

    when(mockApi.sendMessage(chatId, `questionsAmountMessage`, buttons)).thenReturn(
      IO { MessageResponse(ok = true, Result(0L)) }
    )

    messageHandler.unsafeRunSync

    verify(mockApi).sendMessage(chatId, `questionsAmountMessage`, buttons)
  }

  it should "process set question amount message" in {
    val chatId = 3L
    val callbackQuery = CallbackQuery(User(0L, "Dima", None), Some("1 3"), Message(0L, Chat(chatId), None))
    val buttons = List(
      List(InlineKeyboardButton(`allCategoryText`, s"ct 0 $chatId")),
      List(InlineKeyboardButton("\uD83C\uDFF0 История", s"ct 1 $chatId")),
      List(InlineKeyboardButton("\uD83D\uDCDA Литература", s"ct 2 $chatId")),
      List(InlineKeyboardButton("\uD83E\uDDEA Наука", s"ct 3 $chatId")),
      List(InlineKeyboardButton("\uD83D\uDDBC Искусство", s"ct 4 $chatId")),
      List(InlineKeyboardButton("\uD83C\uDFBC Музыка", s"ct 5 $chatId")),
      List(InlineKeyboardButton("\uD83C\uDFA5 Кино", s"ct 6 $chatId")),
      List(InlineKeyboardButton("\uD83E\uDD8B Флора и фауна", s"ct 7 $chatId")),
      List(InlineKeyboardButton("\uD83C\uDF0E География", s"ct 8 $chatId")),
      List(InlineKeyboardButton("\uD83C\uDFC0 Спорт", s"ct 9 $chatId")),
      List(InlineKeyboardButton("\uD83D\uDC60 Мода", s"ct 10 $chatId")),
    )

    when(mockApi.requestUpdates(0)).thenReturn(
      IO { BotResponse(ok = true, List(BotUpdateMessage(0L, None, Some(callbackQuery)))) }
    )

    when(mockApi.editMessage(chatId, 0L,  `questionsAmountMessage`)).thenReturn(
      IO()
    )

    when(mockApi.sendMessage(chatId, `questionsCategoryMessage`, buttons)).thenReturn(
      IO { MessageResponse(ok = true, Result(0L)) }
    )

    messageHandler.unsafeRunSync

    verify(mockApi).sendMessage(chatId, `questionsCategoryMessage`, buttons)
  }

}
