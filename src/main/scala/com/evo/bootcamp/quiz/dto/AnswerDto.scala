package com.evo.bootcamp.quiz.dto

import com.evo.bootcamp.quiz.dao.QuestionsDao.AnswerId
import com.evo.bootcamp.quiz.dto.api.CallbackQuery.User

case class AnswerDto(id: AnswerId, text: String, isRight: Boolean, user: User) extends Comparable[AnswerDto] {
  override def toString: String = s"$text"

  override def compareTo(answer: AnswerDto): Int =
    this.id compareTo answer.id
}
