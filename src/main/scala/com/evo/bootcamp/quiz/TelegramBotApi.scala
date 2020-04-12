package com.evo.bootcamp.quiz


import cats.effect.{Effect, ExitCode, IO, Sync}
import cats.implicits._
import fs2.Stream
import io.chrisdavenport.log4cats.Logger
import org.http4s.client.Client
import org.http4s.{EntityDecoder, Uri}
import com.evo.bootcamp.quiz.dto.{BotResponse, BotUpdate}
import org.http4s.Uri
import org.http4s.implicits._
import cats.effect.{Clock, IO, Timer}
import org.http4s.circe._
import io.circe.generic.auto._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class TelegramBotApi[F[_]](token: String, client: Client[F])(implicit F: Effect[F])
{
  private val botApiUri: Uri = uri"https://api.telegram.org" / s"bot$token"
  implicit val decoder: EntityDecoder[F, BotResponse[List[BotUpdate]]] = jsonOf[F, BotResponse[List[BotUpdate]]]

  def putStrLn(s: BotResponse[List[BotUpdate]]): F[Unit] = F.delay(println(s))

  def requestUpdates(offset: Long): F[Long] = {
    val uri = botApiUri / "getUpdates" =? Map(
      "offset" -> List((offset + 1).toString),
      "timeout" -> List("0.5"), // timeout to throttle the polling
      "allowed_updates" -> List("""["message"]""")
    )
    client.expect[BotResponse[List[BotUpdate]]](uri)
      .map(response => handleCommand(response).getOrElse(offset))
  }

  private def handleCommand(response: BotResponse[List[BotUpdate]]): Option[Long] =
    response.result match {
      case Nil => {
        None
      }
      case nonEmpty => {
        println("hi")
        println(nonEmpty)
        Some(nonEmpty.maxBy(_.update_id).update_id)
      }
    }
}
