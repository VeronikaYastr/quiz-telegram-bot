package com.evo.bootcamp.quiz


import cats.effect.{Effect, ExitCode, IO, Sync}
import cats.implicits._
import io.chrisdavenport.log4cats.Logger
import org.http4s.client.Client
import org.http4s.{EntityDecoder, QueryParamEncoder, Uri}
import com.evo.bootcamp.quiz.dto.{BotMessage, BotResponse, BotUpdate, InlineKeyboardButton}
import org.http4s.implicits._
import cats.effect.{Clock, IO, Timer}
import com.evo.bootcamp.quiz.TelegramBotCommand._
import org.http4s.circe._
import io.circe.generic.auto._
import io.circe.generic.semiauto._
import io.circe.{Encoder, Json}
import io.circe.syntax._
import fs2._
import org.http4s.QueryParamEncoder.stringQueryParamEncoder
import org.http4s.{EntityDecoder, QueryOps, QueryParamEncoder, QueryParameterValue, Uri}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class TelegramBotApi[F[_]](token: String, client: Client[F], logic: TelegramBotLogic[F])(implicit F: Effect[F])
{
  private val botApiUri: Uri = uri"https://api.telegram.org" / s"bot$token"
  implicit val decoder: EntityDecoder[F, BotResponse[List[BotUpdate]]] = jsonOf[F, BotResponse[List[BotUpdate]]]
  implicit val InlineKeyboardButtonEncoder: Encoder[InlineKeyboardButton] = deriveEncoder[InlineKeyboardButton]

  implicit val markupEncoder: QueryParamEncoder[List[InlineKeyboardButton]] =
    (list: List[InlineKeyboardButton]) => {
      QueryParameterValue(s"""{"inline_keyboard": [${list.asJson.noSpaces}]}""")
    }

  def putStrLn(s: BotResponse[List[BotUpdate]]): F[Unit] = F.delay(println(s))

  def requestUpdates(offset: Long):  F[Long] = {
    val uri = botApiUri / "getUpdates" =? Map(
      "offset" -> List((offset + 1).toString),
      "timeout" -> List("0.5"), // timeout to throttle the polling
      "allowed_updates" -> List("""["message"]""")
    )
    client.expect[BotResponse[List[BotUpdate]]](uri)
      .flatMap(response => processMessage(response).map(_.getOrElse(0)))
  }

  def sendMessage(chatId: Long, message: String, buttons: List[InlineKeyboardButton] = List.empty): F[Unit] = {
    val uri = (botApiUri / "sendMessage" =? Map(
      "chat_id" -> List(chatId.toString),
      "parse_mode" -> List("Markdown"),
      "text" -> List(message)
    )) +?? ("reply_markup", Some(buttons).filter(_.nonEmpty))

    client.expect[Unit](uri)
  }

  private def processMessage(response: BotResponse[List[BotUpdate]]): F[Option[Long]] =
    response.result match {
      case Nil =>
        F.pure(None)
      case nonEmpty =>
        val commands: List[F[Unit]] = nonEmpty.flatMap(TelegramBotCommand.fromRawMessage).map(handleCommand)
        commands.sequence.map(_ => Some(nonEmpty.maxBy(_.update_id).update_id))
    }

  def handleCommand(command: TelegramBotCommand): F[Unit] = {
    command match {
      case c: ShowHelp => sendMessage(c.chatId, List(
        "This bot can be used to play quiz game. Commands:",
        s"`$help` - show this help message",
        s"`$start`- starts the game",
        s"`$stop` - stops the game",
      ).mkString("\n"))
      case c: StartGame =>
        val chatId = c.chatId
        // TODO: remove hardcoded amount and send buttons to choose amount
        logic.initGame(10, chatId).void *> sendMessage(chatId, "*Your game is started*")
      case c: Begin =>
        val chatId = c.chatId
        logic.getNextQuestion(chatId).flatMap(x => sendMessage(chatId, x.text))
    }
  }
}
