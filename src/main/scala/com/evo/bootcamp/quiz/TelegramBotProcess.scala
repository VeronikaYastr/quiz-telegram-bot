package com.evo.bootcamp.quiz

import scala.concurrent.ExecutionContext.global
import cats.implicits._
import cats.effect.ConcurrentEffect
import com.evo.bootcamp.quiz.TelegramBotApi.InlineButtons
import com.evo.bootcamp.quiz.TelegramBotCommand.{ChatId, MessageId, QuestionsAmount, QuestionsCategory, ShowHelp, StartGame, UserQuestionAnswer, help, start, stop}
import com.evo.bootcamp.quiz.dao.QuestionsDao.QuestionId
import com.evo.bootcamp.quiz.dto.{AnswerDto, QuestionCategoryDto}
import com.evo.bootcamp.quiz.dto.api.{BotResponse, BotUpdateMessage, InlineKeyboardButton, MessageResponse}
import com.evo.bootcamp.quiz.utils.MessageTexts._

class TelegramBotProcess[F[_]](api: TelegramBotApi[F], logic: TelegramBotLogic[F])(implicit F: ConcurrentEffect[F]) {

  def convertAnswerToButton: (AnswerDto, QuestionId, ChatId) => InlineKeyboardButton = (ans, qId, chatId) =>
    InlineKeyboardButton(s"$ans", s"${ans.id} ${ans.isRight} $qId $chatId")

  def convertCategoryToButton: (QuestionCategoryDto, ChatId) => InlineKeyboardButton = (c, chatId) =>
    InlineKeyboardButton(s"${c.name}", s"ct ${c.id} $chatId")

  def askQuestion(chatId: Long): F[MessageResponse] = {
    val questionsInfo = logic.getQuestionsInfo(chatId)
    questionsInfo.map(info => sendAndCheck(chatId, info.question.id, s"*${info.question}*",
      info.question.answers.grouped(2)
      .map(ansGroup => ansGroup.map(convertAnswerToButton(_, info.question.id, chatId))).toList))
      .sequence
      .flatMap(_ => sendGameResult(chatId))
  }

  def sendAndCheck(chatId: ChatId, questionId: QuestionId, message: String, buttons: InlineButtons = List.empty): F[Unit] = for {
    questionMessage <- api.sendMessage(chatId, message, buttons)
    _               <- F.delay{ Thread.sleep(10000) }
    _               <- api.editMessage(chatId, questionMessage.result.message_id, s"⏱ $message", buttons)
    _               <- F.delay{ Thread.sleep(5000) }
    rightAnswer     = getRightAnswerMessage(chatId, questionId)
    _               <- api.editMessage(chatId, questionMessage.result.message_id, s"$message \n\n $rightAnswer")
    _               <- F.delay{ Thread.sleep(1000) }
  } yield ()

  def sendGameResult(chatId: Long): F[MessageResponse] = {
    var resMessage = `endGameMessage`
    logic.getGameResult(chatId).map(_.sortBy(_.rightAnswersAmount).reverse)
      .foreach(x => x.foreach(g => resMessage += s"\n *${g.username}*: *${g.rightAnswersAmount}* из *${g.totalAmount}*"))
    logic.endGame(chatId)
    api.sendMessage(chatId, resMessage)
  }

  def getRightAnswerMessage(chatId: ChatId, questionId: QuestionId): String = {
    val rightAnswer = logic.getRightAnswer(chatId, questionId).map(_.text).getOrElse("")
    rightAnswerText(rightAnswer)
  }

  def run: F[Long] = {
    def loop(offset: Long): F[Long] = {
      api.requestUpdates(offset)
        .flatMap(response => processMessage(response).map(_.getOrElse(0L)))
        .flatMap(loop)
    }
    loop(0)
  }

  private def processMessage(response: BotResponse[List[BotUpdateMessage]]): F[Option[Long]] =
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
        logic.setQuestionsAmount(c.chatId, c.amount)
        api.editMessage(chatId, c.messageId, `questionsAmountMessage`) *> logic.getAllCategories
          .map(x => x.map(res => List(convertCategoryToButton(res, chatId))))
          .flatMap(x => api
            .sendMessage(chatId,
              `questionsCategoryMessage`,
              List(convertCategoryToButton(QuestionCategoryDto(), chatId)) :: x)).map(_ => ())
      case c: QuestionsCategory =>
        val chatId = c.chatId
        logic.setQuestionsCategory(chatId, c.categoryId)
        for {
          _ <- api.editMessage(chatId, c.messageId, `questionsCategoryMessage`)
          _ <- api.sendMessage(chatId, `startGameMessage`)
          _ <- logic.initGame(chatId)
          _ <- F.start(askQuestion(chatId))
        } yield ()
      case c: UserQuestionAnswer => F.pure(logic.setUserAnswer(c.chatId, c.questionId, c.answer))
      case _ => F.pure(1)
    }
  }
}


