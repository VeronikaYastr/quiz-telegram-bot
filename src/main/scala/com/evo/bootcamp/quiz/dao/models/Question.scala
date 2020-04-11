package com.evo.bootcamp.quiz.dao.models

final case class Question(id: Int, rightAnswer: Int, text: String, category: String, likesCount: Int, disLikesCount: Int) {
  override def toString: String = s"$text ($category)"
}
