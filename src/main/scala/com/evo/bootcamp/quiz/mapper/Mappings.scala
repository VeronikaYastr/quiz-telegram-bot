package com.evo.bootcamp.quiz.mapper

import com.evo.bootcamp.quiz.dao.models.Answer
import com.evo.bootcamp.quiz.dto.AnswerDto
import scala.language.implicitConversions

object Mappings {
  implicit def toAnswerDto(answer: Answer) = AnswerDto(answer.id, answer.text, answer.isRight)
}
