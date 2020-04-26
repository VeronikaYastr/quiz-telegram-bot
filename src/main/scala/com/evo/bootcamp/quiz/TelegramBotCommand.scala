package com.evo.bootcamp.quiz

import com.evo.bootcamp.quiz.TelegramBotCommand.ChatId
import com.evo.bootcamp.quiz.dao.QuestionsDao.QuestionId
import com.evo.bootcamp.quiz.dto.api.{BotUpdateMessage, CallbackQuery, Message}
import com.evo.bootcamp.quiz.dto.AnswerDto

sealed trait TelegramBotCommand {
  val chatId: ChatId
}

object TelegramBotCommand {

  type ChatId = Long

  case class ShowHelp(chatId: ChatId) extends TelegramBotCommand

  case class StartGame(chatId: ChatId) extends TelegramBotCommand

  case class StopGame(chatId: ChatId) extends TelegramBotCommand

  case class UserQuestionAnswer(chatId: ChatId, answer: AnswerDto, questionId: QuestionId) extends TelegramBotCommand

  case class UserQuestionsAmount(chatId: ChatId, amount: Int) extends TelegramBotCommand

  case class QuestionLike(chatId: ChatId, questionId: QuestionId, like: Boolean) extends TelegramBotCommand

  def fromRawMessage(msg: BotUpdateMessage): Option[TelegramBotCommand] = {
    def textCommand: Option[TelegramBotCommand] = msg.message flatMap {
      case Message(chat, Some(`help`)) =>
        Some(ShowHelp(chat.id))
      case Message(chat, Some(`start`)) =>
        Some(StartGame(chat.id))
      case Message(chat, Some(`stop`)) =>
        Some(StopGame(chat.id))
      case _ => None
    }

    def callbackCommand: Option[TelegramBotCommand] = msg.callback_query.collect {
      case CallbackQuery(from, Some(data)) =>
        data.split(" ").toList match {
          case amount :: Nil => Some(UserQuestionsAmount(from.id, amount.toInt))
          case questionId :: userLike :: Nil => Some(QuestionLike(from.id, questionId.toInt, userLike == like))
          case ansId :: ansText :: ansIsRight :: questionId :: Nil => Some(UserQuestionAnswer(from.id, AnswerDto(ansId.toInt, ansText, ansIsRight.toBoolean), questionId.toInt))
          case _ => None
        }
    }.flatten

    callbackCommand orElse textCommand
  }

  val help = "?"
  val start = "/start"
  val stop = "/stop"
  val like = "like"
}
