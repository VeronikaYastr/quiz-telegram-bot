package com.evo.bootcamp.quiz.dto

case class MessageResponse(ok: Boolean, result: Result)

case class Result(message_id: Long)
