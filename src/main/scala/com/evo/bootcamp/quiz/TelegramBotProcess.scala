package com.evo.bootcamp.quiz

import com.evo.bootcamp.quiz.dto.{BotResponse, BotUpdate}

import scala.concurrent.ExecutionContext.global
import cats.implicits._
import cats.effect.{ConcurrentEffect, Effect, ExitCode, IO}

class TelegramBotProcess[F[_]](api: TelegramBotApi[F])(implicit F: Effect[F]) {

  def run: F[Long] = {
    def loop(offset: Long): F[Long] = {
      api.requestUpdates(offset)
        .flatMap(loop)
    }
    loop(0)
  }
}


