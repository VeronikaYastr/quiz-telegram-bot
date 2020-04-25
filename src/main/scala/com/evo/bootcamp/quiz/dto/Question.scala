package com.evo.bootcamp.quiz.dto

case class Question(id: Int, text: String, answers: List[Answer], var userAnswer: Answer) {
  override def toString: String = s"$text"
}
