package com.evo.bootcamp.quiz.dto

final case class BotResponse[T](ok: Boolean, result: T)
