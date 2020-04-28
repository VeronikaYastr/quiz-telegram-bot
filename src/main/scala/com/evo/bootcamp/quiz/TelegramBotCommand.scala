package com.evo.bootcamp.quiz

import com.evo.bootcamp.quiz.TelegramBotCommand._
import com.evo.bootcamp.quiz.dao.QuestionsDao.{CategoryId, QuestionId}
import com.evo.bootcamp.quiz.dto.api.{BotUpdateMessage, CallbackQuery, Message}
import com.evo.bootcamp.quiz.dto.AnswerDto

sealed trait TelegramBotCommand {
  val chatId: ChatId
}

object TelegramBotCommand {

  type ChatId = Long
  type MessageId = Long
  type UserId = Long

  case class ShowHelp(chatId: ChatId) extends TelegramBotCommand

  case class StartGame(chatId: ChatId) extends TelegramBotCommand

  case class StopGame(chatId: ChatId) extends TelegramBotCommand

  case class QuestionsAmount(chatId: ChatId, amount: Int, messageId: MessageId) extends TelegramBotCommand

  case class UserQuestionAnswer(chatId: ChatId, answer: AnswerDto, questionId: QuestionId) extends TelegramBotCommand

  case class QuestionsCategory(chatId: ChatId, categoryId: CategoryId, messageId: MessageId) extends TelegramBotCommand

  def fromRawMessage(msg: BotUpdateMessage): Option[TelegramBotCommand] = {
    def textCommand: Option[TelegramBotCommand] = msg.message flatMap {
      case Message(_, chat, Some(`help`)) =>
        Some(ShowHelp(chat.id))
      case Message(_, chat, Some(`startWithName`) | Some(`start`)) =>
        Some(StartGame(chat.id))
      case Message(_, chat, Some(`stop`) | Some(s"${`stop`}${`botName`}")) =>
        Some(StopGame(chat.id))
      case _ => None
    }

    def callbackCommand: Option[TelegramBotCommand] = msg.callback_query.collect {
      case CallbackQuery(from, Some(data), message) =>
        data.split(" ").toList match {
          case amount :: chatId :: Nil =>
            Some(QuestionsAmount(chatId.toLong, amount.toInt, message.message_id))
          case _ :: id :: chatId :: Nil =>
            Some(QuestionsCategory(chatId.toLong, id.toInt, message.message_id))
          case ansId :: ansIsRight :: questionId :: chatId :: Nil =>
            Some(UserQuestionAnswer(chatId.toLong, AnswerDto(ansId.toInt, "",ansIsRight.toBoolean, from), questionId.toInt))
          case _ => None
        }
    }.flatten

    callbackCommand orElse textCommand
  }

  val help = "?"
  val start = "/start"
  val botName = "@quizzy_funny_bot"
  val startWithName = s"${`start`}${`botName`}"
  val stop = "/stop"
  val like = "like"
}
