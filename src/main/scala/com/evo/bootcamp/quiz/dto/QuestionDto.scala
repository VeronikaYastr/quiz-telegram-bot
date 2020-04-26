package com.evo.bootcamp.quiz.dto

case class QuestionDto(id: Int, text: String, answers: List[AnswerDto], rightAnswer: Option[AnswerDto], var userAnswer: Option[AnswerDto]) {
  override def toString: String = s"$text"
}
