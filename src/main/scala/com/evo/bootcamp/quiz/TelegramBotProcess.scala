package com.evo.bootcamp.quiz

import com.evo.bootcamp.quiz.dto.{BotResponse, BotUpdate, InlineKeyboardButton}

import scala.concurrent.ExecutionContext.global
import cats.implicits._
import cats.effect.{ConcurrentEffect, Effect, ExitCode, Fiber, IO}
import com.evo.bootcamp.quiz.TelegramBotCommand.{ShowHelp, StartGame, UserQuestionsAmount, help, start, stop}

import scala.util.Random

class TelegramBotProcess[F[_]](api: TelegramBotApi[F], logic: TelegramBotLogic[F])(implicit F: ConcurrentEffect[F]) {

  def askQuestion(chatId: Long): F[Unit] = F.delay {
    val findQ = logic.getQuestions(chatId).findLast(_.userAnswer != -1)

    for (_ <- 0 to 5) {
      findQ match {
        case None => api.sendMessage(chatId, "Game is over")
        case Some(value) => api.sendMessage(chatId, value.text,  value.answers.map(x => InlineKeyboardButton(s"${x.text}", s"${x.id} ${value.id}")))
      }
      Thread.sleep(3000)
    }
  }

  def run: F[Long] = {
    def loop(offset: Long): F[Long] = {
      api.requestUpdates(offset)
        .flatMap(response => processMessage(response).map(_.getOrElse(0L)))
        .flatMap(loop)
    }
    loop(0)
  }

  private def processMessage(response: BotResponse[List[BotUpdate]]): F[Option[Long]] =
    response.result match {
      case Nil =>
        F.pure(None)
      case nonEmpty =>
        val commands: List[F[Unit]] = nonEmpty.flatMap(TelegramBotCommand.fromRawMessage).map(handleCommand)
        commands.sequence.map(_ => Some(nonEmpty.maxBy(_.update_id).update_id))
    }

  def handleCommand(command: TelegramBotCommand): F[Unit] = {
    command match {
      case c: ShowHelp => api.sendMessage(c.chatId, List(
        "This bot can be used to play quiz game. Commands:",
        s"`$help` - show this help message",
        s"`$start`- starts the game",
        s"`$stop` - stops the game"
      ).mkString("\n"))
      case c: StartGame =>
        // TODO: remove hardcoded amount and send buttons to choose amount
        api.sendMessage(c.chatId, "*Your game has started. Choose amount of questions*", List(
          InlineKeyboardButton("5", "5"),
          InlineKeyboardButton("10", "10"),
          InlineKeyboardButton("15", "15"),
          InlineKeyboardButton("20", "20")
        ))
      case c: UserQuestionsAmount =>
        val chatId = c.chatId
        logic.initGame(c.amount, chatId) *> F.start(askQuestion(chatId)).map(_ => ExitCode.Success)

//          api.sendMessage(chatId, "Question", List(
//          InlineKeyboardButton("answer", "1 2")
//        ))
    }
  }
}


