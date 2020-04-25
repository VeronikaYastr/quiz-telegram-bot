package com.evo.bootcamp.quiz.dto

case class Answer(id: Int, text: String, isRight: Boolean) extends Comparable[Answer] {
  override def toString: String = s"$text"

  override def compareTo(answer: Answer): Int =
    this.id - answer.id
}
