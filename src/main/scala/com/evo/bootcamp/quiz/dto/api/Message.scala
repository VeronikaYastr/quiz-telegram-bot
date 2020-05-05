package com.evo.bootcamp.quiz.dto.api

import com.evo.bootcamp.quiz.TelegramBotCommand.MessageId

final case class Message(message_id: MessageId, chat: Chat, text: Option[String])
