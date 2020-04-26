package com.evo.bootcamp.quiz.dto.api

final case class BotResponse[T](ok: Boolean, result: T)
