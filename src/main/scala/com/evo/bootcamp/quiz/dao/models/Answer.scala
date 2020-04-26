package com.evo.bootcamp.quiz.dao.models

case class Answer(id: Int, text: String, isRight: Boolean) {
  override def toString: String = s"$text"
}
