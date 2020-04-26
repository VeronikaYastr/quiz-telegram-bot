package com.evo.bootcamp.quiz.dto.api

final case class BotUpdateMessage(update_id: Long, message: Option[Message], callback_query: Option[CallbackQuery])
