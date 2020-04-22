package com.evo.bootcamp.quiz

import cats.effect.Effect
import cats.implicits._
import com.evo.bootcamp.quiz.dao.QuestionsDao
import com.evo.bootcamp.quiz.dao.models.Question

class TelegramBotLogic[F[_]](questionsDao: QuestionsDao[F])(implicit F: Effect[F]) {

  var activeQuestions: Map[Int, List[Question]] = Map[Int, List[Question]]()

  def initGame(amount: Int, chatId: Long): F[Unit] = {
   for {
      gameId <- questionsDao.initGame(chatId, amount)
      questions <- questionsDao.generateRandomQuestions(amount)
      _ = activeQuestions += (gameId -> questions)
    } yield ()
  }

  def getQuestions(chatId: Long): F[List[Question]] = {
    for {
      gameId <- questionsDao.getGameId(chatId)
    } yield activeQuestions.getOrElse(gameId, List.empty)
  }

 /* def getNextQuestion(chatId: Long): F[Question] = {

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
  }*/
}
