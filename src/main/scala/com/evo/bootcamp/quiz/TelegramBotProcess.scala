package com.evo.bootcamp.quiz

import com.evo.bootcamp.quiz.dto.{BotResponse, BotUpdate}

import scala.concurrent.ExecutionContext.global
import cats.implicits._
import cats.effect.{ConcurrentEffect, Effect, ExitCode, IO}
import fs2.Stream
import io.chrisdavenport.log4cats.slf4j._
import io.circe.generic.auto._
import org.http4s._
import org.http4s.circe._
import org.http4s.implicits._
import org.http4s.client.blaze.BlazeClientBuilder

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import cats.effect.IO
import io.circe.Json

class TelegramBotProcess[F[_]](api: TelegramBotApi[F])(implicit F: Effect[F])
{

  def putStrLn(s: String): F[Unit] = F.delay(println(s))

  def run(): F[Unit] =
    Stream
      .repeatEval(api.requestUpdates(0))
      .map(println)
      .compile
      .drain

}




//    BlazeClientBuilder(global).resource.use { client =>
//      for {
//        logger <- Slf4jLogger.create[F]
//        api    <- F.delay( new TelegramBotApi(token, client, logger))
//        _      <- api.runPollUpdates(0)
//      } yield ()
//    }

//    val uri = botApiUri / "getUpdates" =? Map(
//      "offset" -> List((0).toString),
//      "timeout" -> List("0.5"), // timeout to throttle the polling
//      "allowed_updates" -> List("""["message"]""")
//    )
//    BlazeClientBuilder(global).resource.use { client =>
//      client.expect[String](uri) >>= putStrLn
//    }

//    BlazeClientBuilder(global).resource.use { client =>
//      for {
//        logger <- Slf4jLogger.create[F]
//        api    <- F.delay( new TelegramBotApi(token, client, logger))
//      } yield api.pollUpdates(0)
//    }

//    for {
//      client <- BlazeClientBuilder(global).stream
//      logger <- Slf4jLogger.create[F]
//      api    <- F.delay( new TelegramBotApi(token, clien, logger))
//    } yield api.pollUpdates(0)
//  }


