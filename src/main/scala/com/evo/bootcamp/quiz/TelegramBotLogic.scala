package com.evo.bootcamp.quiz

import cats.effect.Effect
import cats.implicits._
import com.evo.bootcamp.quiz.TelegramBotCommand.ChatId
import com.evo.bootcamp.quiz.dao.QuestionsDao._
import com.evo.bootcamp.quiz.dao.QuestionsDao
import com.evo.bootcamp.quiz.dao.models.QuestionWithAnswer
import com.evo.bootcamp.quiz.dto.{AnswerDto, GameResultDto, GameSettingsDto, QuestionCategoryDto, QuestionDto, QuestionInfoDto}
import com.evo.bootcamp.quiz.mapper.Mappings._

class TelegramBotLogic[F[_]](questionsDao: QuestionsDao[F])(implicit F: Effect[F]) {

  var activeQuestions: Map[ChatId, List[QuestionInfoDto]] = Map[ChatId, List[QuestionInfoDto]]()
  var gameSettings: Map[ChatId, GameSettingsDto] = Map[ChatId, GameSettingsDto]()

  def setQuestionsAmount(chatId: ChatId, amount: Int): Unit = {
    val gameSettingsDto = gameSettings.getOrElse(chatId, GameSettingsDto(chatId))
    gameSettingsDto.questionsAmount = amount
    gameSettings += (chatId -> gameSettingsDto)
  }

  def setQuestionsCategory(chatId: ChatId, category: CategoryId): Unit = {
    val gameSettingsDto = gameSettings.getOrElse(chatId, GameSettingsDto(chatId))
    gameSettingsDto.questionsCategory = category
    gameSettings += (chatId -> gameSettingsDto)
  }

  def getAllCategories: F[List[QuestionCategoryDto]] = {
    val questionCategory: F[List[QuestionCategoryDto]] = for {
      x <- questionsDao.getAllCategories.map(toQuestionCategoryDtoList)
    } yield x

    questionCategory
  }

  def initGame(chatId: ChatId): F[Unit] =
    for {
      gameSettings <- F.pure(gameSettings.getOrElse(chatId, GameSettingsDto(chatId)))
      questions <- questionsDao.generateRandomQuestions(gameSettings.questionsAmount, gameSettings.questionsCategory)
      _ = activeQuestions += (chatId -> convertQuestions(questions))
    } yield ()

  private def convertQuestions(questions: List[QuestionWithAnswer]): List[QuestionInfoDto] = {
    val groupedQuestions = questions.groupBy(x => (x.id, x.text))
    groupedQuestions.keys.map {
      case (id, text) =>
        val answers: List[AnswerDto] = groupedQuestions
          .getOrElse((id, text), List.empty)
          .map(_.answer)
        QuestionInfoDto(QuestionDto(id, text, answers), List.empty)
    }.toList
  }

  def getQuestionsInfo(chatId: ChatId): List[QuestionInfoDto] = {
    activeQuestions.getOrElse(chatId, List.empty)
  }

  def getQuestionInfoById(chatId: ChatId, questionId: QuestionId): Option[QuestionInfoDto] = {
    getQuestionsInfo(chatId).find(_.question.id == questionId)
  }

  def getRightAnswer(chatId: ChatId, questionId: QuestionId): Option[AnswerDto] = {
    getQuestionInfoById(chatId, questionId).flatMap(x => x.question.answers.find(_.isRight))
  }

  def getGameResult(chatId: ChatId): Option[List[GameResultDto]] = {
    activeQuestions.get(chatId).map(questions => {
      val userAnswersMap = questions.flatMap(_.userAnswers).groupBy(_.user.username)
      userAnswersMap.keys.flatMap(username => {
        val answers = userAnswersMap.getOrElse(username, List.empty)
        answers.map(_ => GameResultDto(username, answers.count(_.isRight), questions.size))
      }).toList
    })
  }

  def setUserAnswer(chatId: ChatId, questionId: QuestionId, answer: AnswerDto): Unit = {
    getQuestionInfoById(chatId, questionId).foreach(x => {
      x.userAnswers.find(_.user.id == answer.user.id).foreach(ans => {
        x.userAnswers = x.userAnswers.filterNot(_ == ans)
      })
      x.userAnswers ::= answer
    })
  }
}
