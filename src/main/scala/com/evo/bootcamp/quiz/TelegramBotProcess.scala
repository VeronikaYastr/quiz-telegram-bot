package com.evo.bootcamp.quiz

import java.net.URLEncoder
import java.nio.charset.{Charset, StandardCharsets}

import com.evo.bootcamp.quiz.dto.{BotResponse, BotUpdate, InlineKeyboardButton}

import scala.concurrent.ExecutionContext.global
import cats.implicits._
import cats.effect.{ConcurrentEffect, Effect, ExitCode, Fiber, IO}
import com.evo.bootcamp.quiz.TelegramBotCommand.{ShowHelp, StartGame, UserQuestionAnswer, UserQuestionsAmount, help, start, stop}

import scala.util.Random

class TelegramBotProcess[F[_]](api: TelegramBotApi[F], logic: TelegramBotLogic[F])(implicit F: ConcurrentEffect[F]) {

  def askQuestion(chatId: Long): F[List[Unit]] = {
    val questions = logic.getQuestions(chatId)
    questions.map(q => sendAndCheck(chatId, q.text, q.answers.grouped(2).map(ansGroup => ansGroup.map(ans => InlineKeyboardButton(s"${ans.text}", s"${ans.id} ${q.id}"))).toList)).sequence
  }

  def sendAndCheck(chatId: Long, message: String, buttons: List[List[InlineKeyboardButton]] = List.empty): F[Unit] = for {
    _ <- api.sendMessage(chatId, message, buttons)
    _ <- F.delay{ Thread.sleep(6000) }
    _ <- api.sendMessage(chatId, "3 sec left ⌛")
    _ <- F.delay{ Thread.sleep(3000) }
  } yield ()

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
        api.sendMessage(c.chatId, "\uD83E\uDDE9 How many questions do you want to play?", List(
          List(InlineKeyboardButton("5", "5"), InlineKeyboardButton("10", "10")),
          List(InlineKeyboardButton("15", "15"), InlineKeyboardButton("20", "20"))
        ))
      case c: UserQuestionsAmount =>
        val chatId = c.chatId
        for {
          _ <- api.sendMessage(chatId, "Your game is started. \nYou have 10 sec to answer ⏳")
          _ <- logic.initGame(c.amount, chatId)
          _ <- F.start(askQuestion(chatId))
        } yield ()
      case c: UserQuestionAnswer =>
        val rightAnswer = logic.getRightAnswer(c.chatId, c.questionId, c.answerId).map(_.text).getOrElse("")
        val userAnswer = logic.getAnswerById(c.chatId, c.questionId, c.answerId)
        val answer = userAnswer match {
          case None => "no answer \uD83D\uDE41"
          case Some(value) => if (value.isRight) s"${value.text}    ✔" else s"${value.text}    ❌"
        }
        api.sendMessage(c.chatId, s"*Right answer*: $rightAnswer \n*Your answer*: $answer")
        // TODO: remove buttons-answers
      case _ => F.pure(1)
    }
  }
}


