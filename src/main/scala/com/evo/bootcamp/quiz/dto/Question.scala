package com.evo.bootcamp.quiz.dto

final case class Question(id: Int, text: String, answers: List[Answer], userAnswer: Int) {
  override def toString: String = s"$text"
}
