package com.evo.bootcamp.quiz

import com.evo.bootcamp.quiz.dto.{BotResponse, BotUpdate, InlineKeyboardButton}

import scala.concurrent.ExecutionContext.global
import cats.implicits._
import cats.effect.{ConcurrentEffect, Effect, ExitCode, IO}
import com.evo.bootcamp.quiz.TelegramBotCommand.{Begin, ShowHelp, StartGame, UserAnswer, help, start, stop}

class TelegramBotProcess[F[_]](api: TelegramBotApi[F], logic: TelegramBotLogic[F])(implicit F: Effect[F]) {

  def run: F[Long] = {
    def loop(offset: Long): F[Long] = {
      api.requestUpdates(offset)
        .flatMap(response => processMessage(response).map(_.getOrElse(0)))
        .flatMap(loop)
    }
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
        val chatId = c.chatId
        // TODO: remove hardcoded amount and send buttons to choose amount
        logic.initGame(10, chatId) *> api.sendMessage(chatId, "*Your game is started*") *> api.sendMessage(chatId, "Question", List(
          InlineKeyboardButton("answer", "1 2")
        ))
    }
  }
}


