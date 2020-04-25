package com.evo.bootcamp.quiz


import cats.effect.{Clock, ContextShift, Effect, ExitCode, IO, Sync, Timer}
import cats.implicits._
import io.chrisdavenport.log4cats.Logger
import org.http4s.client.Client
import org.http4s.{EntityDecoder, QueryParamEncoder, Uri}
import com.evo.bootcamp.quiz.dto.{BotMessage, BotResponse, BotUpdate, InlineKeyboardButton, MessageResponse}
import org.http4s.implicits._
import com.evo.bootcamp.quiz.TelegramBotCommand._
import org.http4s.circe._
import io.circe.generic.auto._
import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder, Json, HCursor}
import io.circe.syntax._
import fs2._
import org.http4s.QueryParamEncoder.stringQueryParamEncoder
import org.http4s.{EntityDecoder, QueryParamEncoder, QueryParameterValue, Uri}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class TelegramBotApi[F[_]](token: String, client: Client[F], logic: TelegramBotLogic[F])
  (implicit F: Effect[F], contextShift: ContextShift[F])
{
  private val botApiUri: Uri = uri"https://api.telegram.org" / s"bot$token"
  implicit val botUpdatesDecoder: EntityDecoder[F, BotResponse[List[BotUpdate]]] = jsonOf[F, BotResponse[List[BotUpdate]]]

  implicit val messageUpdatesDecoder: EntityDecoder[F, MessageResponse] = jsonOf[F, MessageResponse]
  implicit val InlineKeyboardButtonEncoder: Encoder[InlineKeyboardButton] = deriveEncoder[InlineKeyboardButton]

  implicit val markupEncoder: QueryParamEncoder[List[List[InlineKeyboardButton]]] =
    (list: List[List[InlineKeyboardButton]]) => {
      QueryParameterValue(s"""{"inline_keyboard": ${list.asJson}}""")
    }

  def putStrLn(s: BotResponse[List[BotUpdate]]): F[Unit] = F.delay(println(s))

  def requestUpdates(offset: Long):  F[BotResponse[List[BotUpdate]]] = {
    val uri = botApiUri / "getUpdates" =? Map(
      "offset" -> List((offset + 1).toString),
      "timeout" -> List("0.5"), // timeout to throttle the polling
      "allowed_updates" -> List("""["message", "callback_query"]""")
    )
    client.expect[BotResponse[List[BotUpdate]]](uri)
  }

  def editMessage(chatId: Long, messageId: Long, message: String): F[Unit] = {
    val uri = botApiUri / "getUpdates" =? Map(
      "chat_id" -> List(chatId.toString),
      "message_id" -> List(messageId.toString),
      "parse_mode" -> List("Markdown"),
      "test" -> List(message)
    )
    client.expect[Unit](uri)
  }

  def sendMessage(chatId: Long, message: String, buttons: List[List[InlineKeyboardButton]] = List.empty): F[Unit] = {
    val uri = (botApiUri / "sendMessage" =? Map(
      "chat_id" -> List(chatId.toString),
      "parse_mode" -> List("Markdown"),
      "text" -> List(message)
    )) +?? ("reply_markup", Some(buttons).filter(_.nonEmpty))

    client.expect[MessageResponse](uri)
      .map(println)
  }
}
