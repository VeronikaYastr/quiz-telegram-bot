package com.evo.bootcamp.quiz

import com.evo.bootcamp.quiz.dto.{BotMessage, BotUpdate, CallbackQuery}

sealed trait TelegramBotCommand {
  val chatId: Long
}

object TelegramBotCommand {

  case class ShowHelp(chatId: Long) extends TelegramBotCommand

  //case class InitGame(chatId: Long) extends TelegramBotCommand

  case class StartGame(chatId: Long) extends TelegramBotCommand

  case class UserQuestionAnswer(chatId: Long, answerId: Int, questionId: Int) extends TelegramBotCommand

  case class UserQuestionsAmount(chatId: Long, amount: Int) extends TelegramBotCommand

  def fromRawMessage(msg: BotUpdate): Option[TelegramBotCommand] = {
    def textCommand: Option[TelegramBotCommand] = msg.message flatMap {
      case BotMessage(_, chat, Some(`help`)) =>
        Some(ShowHelp(chat.id))
      case BotMessage(_, chat, Some(`start`)) =>
        Some(StartGame(chat.id))
      case _ => None
    }

    def callbackCommand: Option[TelegramBotCommand] = msg.callback_query.collect {
      case CallbackQuery(from, Some(data)) =>
        data.split(" ").toList match {
          case amount :: Nil => Some(UserQuestionsAmount(from.id, amount.toInt))
          case answerId :: questionId :: Nil => Some(UserQuestionAnswer(from.id, answerId.toInt, questionId.toInt))
          case _ => None
        }
    }.flatten

    callbackCommand orElse textCommand
  }

  val help = "?"
  val start = "/start"
  val stop = "/stop"
}
