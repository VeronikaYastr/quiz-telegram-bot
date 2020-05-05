package com.evo.bootcamp.quiz.dto

final case class GameResultDto(username: String, rightAnswersAmount: Int, totalAmount: Int) {
  override def toString: String = s"$username: $rightAnswersAmount из $totalAmount"
}
