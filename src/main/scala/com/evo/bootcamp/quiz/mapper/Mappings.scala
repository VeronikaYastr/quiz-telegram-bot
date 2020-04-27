package com.evo.bootcamp.quiz.mapper

import com.evo.bootcamp.quiz.dao.models.{Answer, QuestionCategory}
import com.evo.bootcamp.quiz.dto.{AnswerDto, QuestionCategoryDto}
import com.evo.bootcamp.quiz.dto.api.CallbackQuery.User

import scala.language.implicitConversions

object Mappings {
  implicit def toAnswerDto(answer: Answer) = AnswerDto(answer.id, answer.text, answer.isRight, User(0L, ""))

  implicit def toQuestionCategoryDto(category: QuestionCategory): QuestionCategoryDto = QuestionCategoryDto(category.id, category.name)

  implicit def toQuestionCategoryDtoList(category: List[QuestionCategory]): List[QuestionCategoryDto] = category.map(toQuestionCategoryDto)
}
