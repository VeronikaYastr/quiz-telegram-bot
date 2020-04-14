package com.evo.bootcamp.quiz

import cats.effect.Effect
import com.evo.bootcamp.quiz.dao.QuestionsDao
import com.evo.bootcamp.quiz.dao.models.Question
import cats.implicits._

class TelegramBotLogic[F[_]](questionsDao: QuestionsDao[F])(implicit F: Effect[F]) {

  var chatQuestionsMap: Map[Long, List[Question]] = Map()

  def generateQuestions(amount: Int, chatId: Long): F[Unit] = {
    for {
      questions <- questionsDao.generateRandomQuestions(amount)
      _ = chatQuestionsMap += (chatId -> questions)
    } yield ()
  }

  def getNextQuestion(chatId: Long): Option[Question] = {
    val headQuestion = chatQuestionsMap.get(chatId).flatMap(_.headOption)
    chatQuestionsMap += (chatId -> chatQuestionsMap.get(chatId).map(x => if(x.nonEmpty) x.tail else x).getOrElse(List.empty))
    headQuestion
  }
}
