package com.evo.bootcamp.quiz

import cats.effect.Effect
import cats.implicits._
import com.evo.bootcamp.quiz.TelegramBotCommand.ChatId
import com.evo.bootcamp.quiz.dao.QuestionsDao._
import com.evo.bootcamp.quiz.dao.QuestionsDao
import com.evo.bootcamp.quiz.dao.models.QuestionWithAnswer
import com.evo.bootcamp.quiz.dto.{AnswerDto, GameResultDto, QuestionDto, QuestionInfoDto}
import com.evo.bootcamp.quiz.mapper.Mappings._

class TelegramBotLogic[F[_]](questionsDao: QuestionsDao[F])(implicit F: Effect[F]) {

  var activeQuestions: Map[ChatId, List[QuestionInfoDto]] = Map[ChatId, List[QuestionInfoDto]]()

  def initGame(amount: Int, chatId: ChatId): F[Unit] =
    for {
      questions <- questionsDao.generateRandomQuestions(amount)
      _ = activeQuestions += (chatId -> convertQuestions(questions))
    } yield ()

  private def convertQuestions(questions: List[QuestionWithAnswer]): List[QuestionInfoDto] = {
    val groupedQuestions = questions.groupBy(x => (x.id, x.text))
    groupedQuestions.keys.map {
      case (id, text) =>
        val answers: List[AnswerDto] = groupedQuestions
          .getOrElse((id, text), List.empty)
          .map(_.answer)
        QuestionInfoDto(QuestionDto(id, text, answers), answers.find(_.isRight), None)
    }.toList
  }

  def getQuestionsInfo(chatId: ChatId): List[QuestionInfoDto] = {
    activeQuestions.getOrElse(chatId, List.empty)
  }

  def getQuestionInfoById(chatId: ChatId, questionId: QuestionId): Option[QuestionInfoDto] = {
    getQuestionsInfo(chatId).find(_.question.id == questionId)
  }

  def getRightAnswer(chatId: ChatId, questionId: QuestionId): Option[AnswerDto] = {
    getQuestionInfoById(chatId, questionId).flatMap(_.rightAnswer)
  }

  def getGameResult(chatId: ChatId): Option[GameResultDto] = {
    activeQuestions.get(chatId).map(questions => {
      val rightAnswersAmount = questions.count(question =>
        question.rightAnswer.isDefined && question.rightAnswer == question.userAnswer
      )
      GameResultDto(rightAnswersAmount, questions.size)
    })
  }

  def setUserAnswer(chatId: ChatId, questionId: QuestionId, answer: AnswerDto): Unit = {
    getQuestionInfoById(chatId, questionId).foreach(x => x.userAnswer = Some(answer))
  }

  def getUserAnswer(chatId: ChatId, questionId: QuestionId): Option[AnswerDto] = {
    getQuestionInfoById(chatId, questionId).flatMap(_.userAnswer)
  }

  def setQuestionUserLike(chatId: ChatId, questionId: QuestionId, userLike: Boolean): F[Unit] = {
    for {
      likeInfo <- questionsDao.getQuestionsLikeInfo(questionId)
      _ <- questionsDao.setQuestionLikeInfo(questionId, userLike, likeInfo)
    } yield ()
  }
}
