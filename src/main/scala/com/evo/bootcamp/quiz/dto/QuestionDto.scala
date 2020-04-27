package com.evo.bootcamp.quiz.dto

import com.evo.bootcamp.quiz.dao.QuestionsDao.QuestionId

case class QuestionDto(id: QuestionId, text: String, answers: List[AnswerDto]) {
  override def toString: String = s"$text"
}

case class QuestionInfoDto(question: QuestionDto, var userAnswers: List[AnswerDto]) {
  override def toString: String = s"$question"
}
