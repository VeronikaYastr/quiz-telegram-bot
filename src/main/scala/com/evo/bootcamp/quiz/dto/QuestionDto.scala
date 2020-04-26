package com.evo.bootcamp.quiz.dto

case class QuestionDto(id: Int, text: String, answers: List[AnswerDto]) {
  override def toString: String = s"$text"
}

case class QuestionInfoDto(question: QuestionDto, rightAnswer: Option[AnswerDto], var userAnswer: Option[AnswerDto]) {
  override def toString: String = s"$question"
}
