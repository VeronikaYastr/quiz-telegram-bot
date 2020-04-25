package com.evo.bootcamp.quiz.dto

case class Answer(id: Int = -1, text: String = "", var isRight: Option[Boolean] = None) {
  override def toString: String = s"$text"
}
