package com.evo.bootcamp.quiz.dto.api

import com.evo.bootcamp.quiz.dto.api.CallbackQuery.User

final case class CallbackQuery(from: User, data: Option[String])

object CallbackQuery {
  final case class User(id: Long)
}
