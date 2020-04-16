package com.evo.bootcamp.quiz.dto

final case class BotMessage(message_id: Long, chat: Chat, text: Option[String])