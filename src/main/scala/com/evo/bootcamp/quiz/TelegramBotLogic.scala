package com.evo.bootcamp.quiz

import cats.effect.Effect
import cats.implicits._
import com.evo.bootcamp.quiz.dao.QuestionsDao
import com.evo.bootcamp.quiz.dto.{Answer, Question}

class TelegramBotLogic[F[_]](questionsDao: QuestionsDao[F])(implicit F: Effect[F]) {

  var activeQuestions: Map[Long, List[Question]] = Map[Long, List[Question]]()

  def initGame(amount: Int, chatId: Long): F[Unit] = {
   for {
      //_ <- questionsDao.initGame(chatId, amount)
      questions <- questionsDao.generateRandomQuestions(amount)
      qMap = questions.groupBy(x => (x.id, x.text))
      result = qMap.map(_._1).map { case (id, text) => Question(id, text, qMap.getOrElse((id, text), List.empty).map(x => Answer(x.answerId, x.answerText, x.answerIsRight)), -1) }
      _ = activeQuestions += (chatId -> result.toList)
   } yield questions
  }

  def getQuestions(chatId: Long): List[Question] = {
    activeQuestions.getOrElse(chatId, List.empty)
  }

  def getRightAnswer(chatId: Long, questionId: Int, answerId: Int): Option[Answer] = {
    val questions = activeQuestions.getOrElse(chatId, List.empty)
    questions.find(_.id == questionId).map(_.answers).flatMap(a => a.find(_.isRight))
  }

  def getAnswerById(chatId: Long, questionId: Int, answerId: Int): Option[Answer] = {
    val questions = activeQuestions.getOrElse(chatId, List.empty)
    questions.find(_.id == questionId).map(_.answers).flatMap(a => a.find(_.id == answerId))
  }
}
