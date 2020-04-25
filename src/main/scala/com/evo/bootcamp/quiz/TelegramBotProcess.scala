package com.evo.bootcamp.quiz

import java.net.URLEncoder
import java.nio.charset.{Charset, StandardCharsets}

import com.evo.bootcamp.quiz.dto.{BotResponse, BotUpdate, InlineKeyboardButton, MessageResponse}

import scala.concurrent.ExecutionContext.global
import cats.implicits._
import cats.effect.{ConcurrentEffect, Effect, ExitCode, Fiber, IO}
import com.evo.bootcamp.quiz.TelegramBotCommand.{QuestionLike, ShowHelp, StartGame, UserQuestionAnswer, UserQuestionsAmount, help, start, stop}

import scala.util.Random

class TelegramBotProcess[F[_]](api: TelegramBotApi[F], logic: TelegramBotLogic[F])(implicit F: ConcurrentEffect[F]) {

  def getLikeButtons(questionId: Int): List[List[InlineKeyboardButton]] = List(List(InlineKeyboardButton("\uD83D\uDC4D", s"$questionId like"), InlineKeyboardButton("\uD83D\uDC4E", s"$questionId dislike")))

  def askQuestion(chatId: Long): F[List[Unit]] = {
    val questions = logic.getQuestions(chatId)
    questions.map(q => sendAndCheck(chatId, q.id, s"*${q.text}*", q.answers.grouped(2)
      .map(ansGroup => ansGroup.map(ans => InlineKeyboardButton(s"${ans.text}", s"${ans.id} ${ans.text} ${ans.isRight.getOrElse(false)} ${q.id}"))).toList))
      .sequence
  }

  def sendAndCheck(chatId: Long, questionId: Int, message: String, buttons: List[List[InlineKeyboardButton]] = List.empty): F[Unit] = for {
    questionMessage <- api.sendMessage(chatId, message, buttons)
    _               <- F.delay{ Thread.sleep(6000) }
    reminderMessage <- api.sendMessage(chatId, "_осталось 3 секунды_ ⌛")
    _               <- F.delay{ Thread.sleep(3000) }
    _               <- api.deleteMessage(chatId, reminderMessage.result.message_id)
    _               <- api.editMessage(chatId, questionMessage.result.message_id, message, getLikeButtons(questionId))
    _               <- resolveUserAnswer(chatId, questionId)
    _               <- F.delay{ Thread.sleep(1000) }
  } yield ()

  def resolveUserAnswer(chatId: Long, questionId: Int): F[MessageResponse] = {
    val userAnswer = logic.getUserAnswer(chatId, questionId)
    val answerText = userAnswer.map(_.text).getOrElse("")
    val rightAnswer = logic.getRightAnswer(chatId, questionId).map(_.text).getOrElse("")
    val resAnswer = {
      userAnswer.flatMap(_.isRight) match {
        case None => "нет ответа \uD83D\uDE41"
        case Some(value) => if (value) s"${answerText}    ✔" else s"${answerText}    ❌"
      }
    }
    api.sendMessage(chatId, s"_Правильный ответ_: $rightAnswer \n_Ваш ответ_: $resAnswer")
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
      ).mkString("\n")).map(_ => ())
      case c: StartGame =>
        api.sendMessage(c.chatId, "\uD83E\uDDE9 Выберите количество вопросов в игре", List(
          List(InlineKeyboardButton("5", "5"), InlineKeyboardButton("10", "10")),
          List(InlineKeyboardButton("15", "15"), InlineKeyboardButton("20", "20"))
        )).map(_ => ())
      case c: UserQuestionsAmount =>
        val chatId = c.chatId
        for {
          _ <- api.sendMessage(chatId, "Ваша игра началась. \nУ вас есть *10 секунд* на ответ ⏳")
          _ <- logic.initGame(c.amount, chatId)
          _ <- F.start(askQuestion(chatId))
        } yield ()
      case c: UserQuestionAnswer => F.pure(logic.setUserAnswer(c.chatId, c.questionId, c.answer))
      case c: QuestionLike => logic.setQuestionUserLike(c.chatId, c.questionId, c.like)
      case _ => F.pure(1)
    }
  }
}


