package com.evo.bootcamp.quiz.dto

case class AnswerDto(id: Int, text: String, isRight: Boolean) extends Comparable[AnswerDto] {
  override def toString: String = s"$text"

  override def compareTo(answer: AnswerDto): Int =
    this.id - answer.id
}
