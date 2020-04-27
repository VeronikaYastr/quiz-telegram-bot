package com.evo.bootcamp.quiz.dto.api

import com.evo.bootcamp.quiz.TelegramBotCommand.UserId
import com.evo.bootcamp.quiz.dto.api.CallbackQuery.User

final case class CallbackQuery(from: User, data: Option[String], message: Message)

object CallbackQuery {
  final case class User(id: UserId, username: String)
}
