package com.evo.bootcamp.quiz.dto

case class Question(id: Int, text: String, answers: List[Answer], rightAnswer: Option[Answer], var userAnswer: Option[Answer]) {
  override def toString: String = s"$text"
}
