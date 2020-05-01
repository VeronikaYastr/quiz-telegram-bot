package com.evo.bootcamp.quiz

import cats.effect.concurrent.Ref
import cats.effect.{ExitCode, IO, IOApp}
import com.evo.bootcamp.quiz.TelegramBotCommand.ChatId
import com.evo.bootcamp.quiz.dto.GameDto
import com.evo.bootcamp.quiz.dto.GameDto.GameSettingsDto

object Test extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    val ref = Ref[IO].of(Map.empty[ChatId, GameDto])
    ref.flatMap(_.update(_ + (1L -> GameDto(GameSettingsDto(1L))))).unsafeRunSync()
    for {
      r <- ref
      _ <- r.update(_ + (1L -> GameDto(GameSettingsDto(1L))))
      res <- r.get
      _ = print(res.get(1L).map(_.questions))
    } yield ExitCode.Success
    /*for {
      ref <- Ref[IO].of(Map.empty[ChatId, GameDto])
      _ <- ref.update(_ + (1L -> GameDto(GameSettingsDto(1L))))
      r <- ref.get
      _ = println(r.get(1L).map(_.questions))
    } yield ExitCode.Success*/
  }
}
