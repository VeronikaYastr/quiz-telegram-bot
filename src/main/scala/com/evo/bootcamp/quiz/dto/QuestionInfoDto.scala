package com.evo.bootcamp.quiz.dto

import com.evo.bootcamp.quiz.dao.QuestionsDao.QuestionId
import com.evo.bootcamp.quiz.dto.QuestionInfoDto.QuestionDto

case class QuestionInfoDto(question: QuestionDto, var userAnswers: List[AnswerDto]) {
  override def toString: String = s"$question"
}

object QuestionInfoDto {

  case class QuestionDto(id: QuestionId, text: String, answers: List[AnswerDto]) {
    override def toString: String = s"$text"
  }

}
