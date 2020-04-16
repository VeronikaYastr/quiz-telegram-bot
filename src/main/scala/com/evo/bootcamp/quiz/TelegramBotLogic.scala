package com.evo.bootcamp.quiz

import cats.effect.Effect
import cats.implicits._
import com.evo.bootcamp.quiz.dao.QuestionsDao
import com.evo.bootcamp.quiz.dao.models.Question

class TelegramBotLogic[F[_]](questionsDao: QuestionsDao[F])(implicit F: Effect[F]) {

  def initGame(amount: Int, chatId: Long): F[Unit] = {
    questionsDao.initGame(chatId, amount).void
  }

  def getNextQuestion(chatId: Long): F[Question] = {

    def generateAndCheck: F[Question] = for {
      question <- questionsDao.generateRandomQuestion()
      gameId <- questionsDao.getGameId(chatId)
      qId <- questionsDao.checkUniqueQuestion(chatId, gameId, question.id)
      _ = qId match {
        case None => questionsDao.saveQuestionForUser(question.id, chatId, gameId)
        case Some(_) => generateAndCheck
      }
    } yield question

    generateAndCheck
  }
}
