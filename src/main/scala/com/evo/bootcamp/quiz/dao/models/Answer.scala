package com.evo.bootcamp.quiz.dao.models

import com.evo.bootcamp.quiz.dao.QuestionsDao.AnswerId

case class Answer(id: AnswerId, text: String, isRight: Boolean) {
  override def toString: String = s"$text"
}
