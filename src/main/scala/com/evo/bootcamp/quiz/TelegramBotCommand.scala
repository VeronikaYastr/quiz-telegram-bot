package com.evo.bootcamp.quiz

import com.evo.bootcamp.quiz.dto.{BotMessage, BotUpdate}

sealed trait TelegramBotCommand {
  val chatId: Long
}

object TelegramBotCommand {

  case class ShowHelp(chatId: Long) extends TelegramBotCommand

  case class StartGame(chatId: Long) extends TelegramBotCommand

  case class Begin(chatId: Long) extends TelegramBotCommand

  def fromRawMessage(msg: BotUpdate): Option[TelegramBotCommand] = {
    def textCommand: Option[TelegramBotCommand] = msg.message flatMap {
      case BotMessage(_, chat, Some(`help`)) =>
        Some(ShowHelp(chat.id))
      case BotMessage(_, chat, Some(`start`)) =>
        Some(StartGame(chat.id))
      case BotMessage(_, chat, Some(`begin`)) =>
        Some(Begin(chat.id))
      case _ => None
    }

    textCommand
  }

  val help = "?"
  val start = "/start"
  val begin = "/begin"
  val stop = "/stop"
}
