package com.evo.bootcamp.quiz

import cats.effect.Effect
import cats.implicits._
import com.evo.bootcamp.quiz.dao.QuestionsDao
import com.evo.bootcamp.quiz.dao.models.Question

class TelegramBotLogic[F[_]](questionsDao: QuestionsDao[F])(implicit F: Effect[F]) {

  def initGame(amount: Int, chatId: Long): F[List[Int]] = {
    val res = for {
      gameId <- questionsDao.initGame(chatId, amount)
      questions <- questionsDao.generateRandomQuestions(amount)
    } yield questions.map(question => questionsDao.saveQuestionForUser(question.id, chatId, gameId))
    res.flatMap(_.sequence)
  }

  def getNewQuestion(chatId: Long): F[Question] = {
    for {
      gameId <- questionsDao.getGameId(chatId)
      questionId <- questionsDao.getQuestionId(chatId, gameId)
      question <- questionsDao.getQuestion(questionId)
    } yield question
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
