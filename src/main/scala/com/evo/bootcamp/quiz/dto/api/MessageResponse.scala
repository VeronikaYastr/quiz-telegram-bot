package com.evo.bootcamp.quiz.dto.api

import com.evo.bootcamp.quiz.dto.api.MessageResponse.Result

case class MessageResponse(ok: Boolean, result: Result)

object MessageResponse {
  case class Result(message_id: Long)
}
