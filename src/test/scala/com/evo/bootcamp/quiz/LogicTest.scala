package com.evo.bootcamp.quiz

import cats.effect.concurrent.Ref
import cats.effect.{IO, _}
import com.evo.bootcamp.quiz.TelegramBotApi.InlineButtons
import com.evo.bootcamp.quiz.TelegramBotCommand.{ChatId, _}
import com.evo.bootcamp.quiz.config.DbConfig
import com.evo.bootcamp.quiz.dao.{DaoInit, QuestionsDao}
import com.evo.bootcamp.quiz.dto.GameDto
import com.evo.bootcamp.quiz.dto.api.CallbackQuery.User
import com.evo.bootcamp.quiz.dto.api.MessageResponse.Result
import com.evo.bootcamp.quiz.dto.api._
import com.evo.bootcamp.quiz.utils.MessageTexts._
import org.mockito.{ArgumentMatchersSugar, MockitoSugar}
import org.scalatest._
import org.scalatest.flatspec.AnyFlatSpec

import scala.concurrent.ExecutionContext

class LogicTest extends AnyFlatSpec with BeforeAndAfter with MockitoSugar with ArgumentMatchersSugar {
  implicit val contextShift = IO.contextShift(ExecutionContext.global)
  implicit val timer: Timer[IO] = IO.timer(ExecutionContext.global)
  val mockApi: TelegramBotApi[IO] = mock[TelegramBotApi[IO]]

  val successfulMessageResponse = IO {
    MessageResponse(ok = true, Result(0L))
  }

  val messageHandler: IO[MessageId] = for {
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
      IO {
        BotResponse(ok = true, List(BotUpdateMessage(0L, Some(Message(0L, Chat(chatId), Some(`help`))), None)))
      }
    )
    when(mockApi.sendMessage(chatId, `helpMessage`)).thenReturn(
      successfulMessageResponse
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
      IO {
        BotResponse(ok = true, List(BotUpdateMessage(0L, Some(Message(2L, Chat(chatId), Some(`start`))), None)))
      }
    )

    when(mockApi.sendMessage(chatId, `questionsAmountMessage`, buttons)).thenReturn(
      successfulMessageResponse
    )

    messageHandler.unsafeRunSync

    verify(mockApi).sendMessage(chatId, `questionsAmountMessage`, buttons)
  }

  it should "process set question amount message" in {
    val chatId = 3L
    val amount = 1
    val callbackQuery = CallbackQuery(User(0L, "Dima", None),
      Some(s"$amount $chatId"),
      Message(0L, Chat(chatId), None))
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
      List(InlineKeyboardButton("\uD83E\uDDF8 Детские", s"ct 11 $chatId")),
    )

    when(mockApi.requestUpdates(0)).thenReturn(
      IO {
        BotResponse(ok = true, List(BotUpdateMessage(0L, None, Some(callbackQuery))))
      }
    )

    when(mockApi.editMessage(chatId, 0L, `questionsAmountMessage`)).thenReturn(
      IO(())
    )

    when(mockApi.sendMessage(chatId, `questionsCategoryMessage`, buttons)).thenReturn(
      successfulMessageResponse
    )

    messageHandler.unsafeRunSync

    verify(mockApi).sendMessage(chatId, `questionsCategoryMessage`, buttons)
    verify(mockApi).editMessage(chatId, 0L, `questionsAmountMessage`)
  }

  it should "process set question category message" in {
    val chatId = 4L
    val category = 2
    val callbackQuery = CallbackQuery(User(0L, "Dima", None),
      Some(s"ct $category $chatId"),
      Message(0L, Chat(chatId), None))

    when(mockApi.requestUpdates(0)).thenReturn(
      IO {
        BotResponse(ok = true, List(BotUpdateMessage(0L, None, Some(callbackQuery))))
      }
    )

    when(mockApi.editMessage(
      eqTo(chatId),
      eqTo(0L),
      eqTo(`questionsCategoryMessage`),
      eqTo(List.empty)))
      .thenReturn(
        IO(())
      )

    when(mockApi.sendMessage(eqTo(chatId), any[String], any[InlineButtons]))
      .thenReturn(
        successfulMessageResponse
      )

    when(mockApi.editMessage(eqTo(chatId), eqTo(0L), any[String], any[InlineButtons])).thenReturn(
      IO(())
    )

    messageHandler.unsafeRunSync

    verify(mockApi).sendMessage(eqTo(chatId), eqTo(`startGameMessage`), eqTo(List.empty))
    verify(mockApi, times(10)).sendMessage(eqTo(chatId), any[String], any[InlineButtons])
  //  verify(mockApi).editMessage(eqTo(chatId), eqTo(0L), eqTo(`questionsCategoryMessage`), eqTo(List.empty))
 //   verify(mockApi, times(10)).editMessage(eqTo(chatId), eqTo(0L), any[String], any[InlineButtons])
  }

}
