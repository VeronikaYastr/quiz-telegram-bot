package com.evo.bootcamp.quiz.dto

final case class BotUpdate(update_id: Long, message: Option[BotMessage], callback_query: Option[CallbackQuery])
