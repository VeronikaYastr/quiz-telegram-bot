package com.evo.bootcamp.quiz

import cats.effect.{ContextShift, Effect}
import cats.implicits._
import com.evo.bootcamp.quiz.TelegramBotApi.InlineButtons
import org.http4s.client.Client
import org.http4s.implicits._
import com.evo.bootcamp.quiz.dto.api.{BotResponse, BotUpdateMessage, InlineKeyboardButton, MessageResponse}
import org.http4s.circe._
import io.circe.generic.auto._
import io.circe.generic.semiauto._
import io.circe.Encoder
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
  implicit val botUpdatesDecoder: EntityDecoder[F, BotResponse[List[BotUpdateMessage]]] = jsonOf[F, BotResponse[List[BotUpdateMessage]]]

  implicit val messageUpdatesDecoder: EntityDecoder[F, MessageResponse] = jsonOf[F, MessageResponse]
  implicit val InlineKeyboardButtonEncoder: Encoder[InlineKeyboardButton] = deriveEncoder[InlineKeyboardButton]

  implicit val markupEncoder: QueryParamEncoder[InlineButtons] =
    (list: InlineButtons) => {
      QueryParameterValue(s"""{"inline_keyboard": ${list.asJson}}""")
    }

  def requestUpdates(offset: Long):  F[BotResponse[List[BotUpdateMessage]]] = {
    val uri = botApiUri / "getUpdates" =? Map(
      "offset" -> List((offset + 1).toString),
      "timeout" -> List("0.5"),
      "allowed_updates" -> List("""["message", "callback_query"]""")
    )
    client.expect[BotResponse[List[BotUpdateMessage]]](uri)
  }

  def editMessage(chatId: Long, messageId: Long, message: String, buttons: InlineButtons = List.empty): F[Unit] = {
    val uri = (botApiUri / "editMessageText" =? Map(
      "chat_id" -> List(chatId.toString),
      "message_id" -> List(messageId.toString),
      "parse_mode" -> List("Markdown"),
      "text" -> List(message)
    )) +?? ("reply_markup", Some(buttons).filter(_.nonEmpty))
    client.expect[Unit](uri)
  }

  def deleteMessage(chatId: Long, messageId: Long): F[Unit] = {
    val uri = botApiUri / "deleteMessage" =? Map(
      "chat_id" -> List(chatId.toString),
      "message_id" -> List(messageId.toString)
    )
    client.expect[Unit](uri)
  }

  def sendMessage(chatId: Long, message: String, buttons: InlineButtons = List.empty): F[MessageResponse] = {
    val uri = (botApiUri / "sendMessage" =? Map(
      "chat_id" -> List(chatId.toString),
      "parse_mode" -> List("Markdown"),
      "text" -> List(message)
    )) +?? ("reply_markup", Some(buttons).filter(_.nonEmpty))

    client.expect[MessageResponse](uri)
  }
}

object TelegramBotApi {
  type InlineButtons = List[List[InlineKeyboardButton]]
}
