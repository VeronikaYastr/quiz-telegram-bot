package com.evo.bootcamp.quiz.dto

final case class Answer(id: Int, text: String, isRight: Boolean) {
  override def toString: String = s"$text"
}
