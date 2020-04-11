package com.evo.bootcamp.quiz.dto

case class BotResponse[T](ok: Boolean, result: T)
