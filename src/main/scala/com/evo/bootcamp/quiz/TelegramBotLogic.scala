package com.evo.bootcamp.quiz

import cats.effect.Effect
import com.evo.bootcamp.quiz.dao.QuestionsDao
import com.evo.bootcamp.quiz.dao.models.Question
import cats.implicits._

class TelegramBotLogic[F[_]](questionsDao: QuestionsDao[F])(implicit F: Effect[F]) {

  def setQuestionsAmount(amount: Int, chatId: Long): F[Unit] = {
    for {
      question <- questionsDao.generateRandomQuestion()
      //TODO: check if question is unique
      _ <- questionsDao.initGame(chatId, question.id, amount)
    } yield ()
  }

  def getNextQuestion(chatId: Long): Option[Question] = {

  }
}
