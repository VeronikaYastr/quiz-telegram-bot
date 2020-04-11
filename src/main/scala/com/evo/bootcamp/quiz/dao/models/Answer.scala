package com.evo.bootcamp.quiz.dao.models

final case class Answer(id: Int, questionId: Int, text: String) {
  override def toString: String = s"$text"
}
