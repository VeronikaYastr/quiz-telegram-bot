package com.evo.bootcamp.quiz.dto

final case class CallbackQuery(from: User, data: Option[String])