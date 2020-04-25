package com.evo.bootcamp.quiz

import cats.effect.Effect
import cats.implicits._
import com.evo.bootcamp.quiz.dao.QuestionsDao
import com.evo.bootcamp.quiz.dao.models.QuestionWithAnswer
import com.evo.bootcamp.quiz.dto.{Answer, GameResult, Question}

class TelegramBotLogic[F[_]](questionsDao: QuestionsDao[F])(implicit F: Effect[F]) {

  var activeQuestions: Map[Long, List[Question]] = Map[Long, List[Question]]()

  def initGame(amount: Int, chatId: Long): F[Unit] =
    for {
      questions <- questionsDao.generateRandomQuestions(amount)
      _ = activeQuestions += (chatId -> convertQuestions(questions))
    } yield ()


  private def convertQuestions(questions: List[QuestionWithAnswer]): List[Question] = {
    val groupedQuestions = questions.groupBy(x => (x.id, x.text))
    groupedQuestions.keys.map {
      case (id, text) =>
        val answers = groupedQuestions
          .getOrElse((id, text), List.empty)
          .map(x => Answer(x.answerId, x.answerText, x.answerIsRight))
        Question(id, text, answers, answers.find(answer => answer.isRight), None)
    }.toList
  }

  def getRightAnswer(chatId: Long, questionId: Int): Option[Answer] = {
    val questions = activeQuestions.getOrElse(chatId, List.empty)
    questions.find(_.id == questionId).flatMap(_.rightAnswer)
  }

  def getResult(chatId: Long): Option[GameResult] = {
    activeQuestions.get(chatId) match {
      case Some(questions) =>
        val rightAnswersAmount = questions.count(question =>
          question.rightAnswer.isDefined && question.rightAnswer == question.userAnswer
        )
        Some(GameResult(rightAnswersAmount, questions.size))
      case _ => None
    }
  }

  def getQuestions(chatId: Long): List[Question] = {
    activeQuestions.getOrElse(chatId, List.empty)
  }

  def setUserAnswer(chatId: Long, questionId: Int, answer: Answer): Unit = {
    val questions = activeQuestions.getOrElse(chatId, List.empty)
    questions.find(_.id == questionId).foreach(x => x.userAnswer = Some(answer))
  }

  def getUserAnswer(chatId: Long, questionId: Int): Option[Answer] = {
    val questions = activeQuestions.getOrElse(chatId, List.empty)
    questions.find(_.id == questionId).flatMap(_.userAnswer)
  }

  def setQuestionUserLike(chatId: Long, questionId: Int, userLike: Boolean): F[Unit] = {
    for {
      likeInfo <- questionsDao.getQuestionsLikeInfo(questionId)
      _ <- questionsDao.setQuestionLikeInfo(questionId, userLike, likeInfo)
    } yield ()
  }
}
