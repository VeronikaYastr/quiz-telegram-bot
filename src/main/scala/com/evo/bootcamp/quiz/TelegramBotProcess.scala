package com.evo.bootcamp.quiz

import cats.effect.concurrent.Ref
import cats.effect.{ConcurrentEffect, Fiber, Timer}
import cats.implicits._
import com.evo.bootcamp.quiz.TelegramBotApi.InlineButtons
import com.evo.bootcamp.quiz.TelegramBotCommand._
import com.evo.bootcamp.quiz.dao.QuestionsDao.QuestionId
import com.evo.bootcamp.quiz.dto.api.{BotResponse, BotUpdateMessage, InlineKeyboardButton, MessageResponse}
import com.evo.bootcamp.quiz.dto.{AnswerDto, QuestionCategoryDto}
import com.evo.bootcamp.quiz.utils.MessageTexts._

import scala.concurrent.duration._

class TelegramBotProcess[F[_]](api: TelegramBotApi[F], logic: TelegramBotLogic[F],
                               fiberRef: Ref[F, Map[ChatId, Fiber[F, MessageResponse]]])
                              (implicit F: ConcurrentEffect[F], timer: Timer[F]) {

  def convertAnswerToButton: (AnswerDto, QuestionId, ChatId) => InlineKeyboardButton = (ans, qId, chatId) =>
    InlineKeyboardButton(s"$ans", s"${ans.id} ${ans.isRight} $qId $chatId")

  def convertCategoryToButton: (QuestionCategoryDto, ChatId) => InlineKeyboardButton = (c, chatId) =>
    InlineKeyboardButton(s"${c.name}", s"ct ${c.id} $chatId")

  def askQuestion(chatId: Long): F[MessageResponse] = {
    logic.getQuestionsInfo(chatId).flatMap(x =>
      x.map(info => sendAndCheck(chatId, info.question.id, s"*${info.question}*", info.question.answers.grouped(2)
        .map(ansGroup => ansGroup.map(convertAnswerToButton(_, info.question.id, chatId))).toList))
        .sequence
        .flatMap(_ => sendGameResult(chatId)))
  }

  def sendAndCheck(chatId: ChatId, questionId: QuestionId, message: String, buttons: InlineButtons = List.empty): F[Unit] = for {
    questionMessage <- api.sendMessage(chatId, message, buttons)
    _ <- timer.sleep(10.seconds)
    _ <- api.editMessage(chatId, questionMessage.result.message_id, s"⏱ $message", buttons)
    _ <- timer.sleep(5.seconds)
    rightAnswer <- getRightAnswerMessage(chatId, questionId)
    _ <- api.editMessage(chatId, questionMessage.result.message_id, s"$message \n\n $rightAnswer")
    _ <- timer.sleep(1.seconds)
  } yield ()

  def sendGameResult(chatId: Long): F[MessageResponse] = {
    val startMessage = List(`endGameMessage`)
    for {
      gameResult <- logic.getGameResult(chatId)
      finalMessage = gameResult.map(_.sortBy(_.rightAnswersAmount).reverse)
        .map(l => l.flatMap(g => s"\n *${g.username}*: *${g.rightAnswersAmount}* из *${g.totalAmount}*" :: startMessage))
      _ <- logic.endGame(chatId)
      res <- api.sendMessage(chatId, finalMessage.map(_.reverse.mkString("\n")).getOrElse(""))
    } yield res
  }

  def getRightAnswerMessage(chatId: ChatId, questionId: QuestionId): F[String] = {
    for {
      rightAnswer <- logic.getRightAnswer(chatId, questionId)
    } yield rightAnswerText(rightAnswer.map(_.text).getOrElse(""))
  }

  def run: F[Long] = {
    def loop(offset: Long): F[Long] = {
      api.requestUpdates(offset)
        .flatMap(response => processMessage(response).map(_.getOrElse(0L)))
        .flatMap(loop)
    }

    loop(0)
  }

  def processMessage(response: BotResponse[List[BotUpdateMessage]]): F[Option[Long]] =
    response.result match {
      case Nil =>
        F.pure(None)
      case nonEmpty =>
        val commands: List[F[Unit]] = nonEmpty.flatMap(TelegramBotCommand.fromRawMessage).map(handleCommand)
        commands.sequence.map(_ => Some(nonEmpty.maxBy(_.update_id).update_id))
    }

  def handleCommand(command: TelegramBotCommand): F[Unit] = {
    command match {
      case c: ShowHelp => api.sendMessage(c.chatId, `helpMessage`).map(_ => ())
      case c: StartGame =>
        val chatId = c.chatId
        api.sendMessage(chatId, `questionsAmountMessage`, List(
          List(InlineKeyboardButton("5", s"5 $chatId"), InlineKeyboardButton("10", s"10 $chatId")),
          List(InlineKeyboardButton("15", s"15 $chatId"), InlineKeyboardButton("20", s"20 $chatId"))
        )).map(_ => ())
      case c: QuestionsAmount =>
        val chatId = c.chatId
        for {
          _ <- logic.setQuestionsAmount(c.chatId, c.amount)
          _ <- api.editMessage(chatId, c.messageId, `questionsAmountMessage`)
          _ <- logic.getAllCategories
            .map(x => x.map(res => List(convertCategoryToButton(res, chatId))))
              .flatMap(x => api
              .sendMessage(chatId,
                `questionsCategoryMessage`,
                List(convertCategoryToButton(QuestionCategoryDto(), chatId)) :: x)).map(_ => ())
        } yield ()
      case c: QuestionsCategory =>
        val chatId = c.chatId
        for {
          _ <- logic.setQuestionsCategory(chatId, c.categoryId)
          _ <- api.editMessage(chatId, c.messageId, `questionsCategoryMessage`)
          _ <- api.sendMessage(chatId, `startGameMessage`)
          _ <- logic.initGame(chatId)
          fiber <- F.start(askQuestion(chatId))
          _ <- fiberRef.update(_ + (chatId -> fiber))
        } yield ()
      case c: UserQuestionAnswer => logic.setUserAnswer(c.chatId, c.questionId, c.answer)
      case c: StopGame =>
        for {
          _ <- fiberRef.get.flatMap(x => x.get(c.chatId) match {
            case Some(value) => value.cancel
            case None => F.pure(())
          })
        } yield ()
    }
  }
}


