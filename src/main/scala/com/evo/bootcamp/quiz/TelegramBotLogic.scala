package com.evo.bootcamp.quiz

import cats.effect.Effect
import cats.implicits._
import com.evo.bootcamp.quiz.dao.QuestionsDao
import com.evo.bootcamp.quiz.dto.{Answer, Question}

class TelegramBotLogic[F[_]](questionsDao: QuestionsDao[F])(implicit F: Effect[F]) {

  var activeQuestions: Map[Long, List[Question]] = Map[Long, List[Question]]()

  def initGame(amount: Int, chatId: Long): F[Unit] = {
   for {
      _ <- questionsDao.initGame(chatId, amount)
      questions <- questionsDao.generateRandomQuestions(amount)
      qMap = questions.groupBy(_.id)
      result = questions.map(x => Question(x.id, x.text, qMap.getOrElse(x.id, List.empty).map(x => Answer(x.id, x.answerText, x.answerIsRight)), -1))
      _ = activeQuestions += (chatId -> result)
   } yield questions
  }

  def getQuestions(chatId: Long): List[Question] = {
    activeQuestions.getOrElse(chatId, List.empty)
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
