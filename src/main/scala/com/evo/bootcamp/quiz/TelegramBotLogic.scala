package com.evo.bootcamp.quiz

import cats.effect.Effect
import cats.effect.concurrent.Ref
import cats.implicits._
import com.evo.bootcamp.quiz.TelegramBotCommand.ChatId
import com.evo.bootcamp.quiz.dao.QuestionsDao
import com.evo.bootcamp.quiz.dao.QuestionsDao._
import com.evo.bootcamp.quiz.dao.models.QuestionWithAnswer
import com.evo.bootcamp.quiz.dto.GameDto.GameSettingsDto
import com.evo.bootcamp.quiz.dto.QuestionInfoDto.QuestionDto
import com.evo.bootcamp.quiz.dto._
import com.evo.bootcamp.quiz.mapper.Mappings._

class TelegramBotLogic[F[_]](questionsDao: QuestionsDao[F], ref: Ref[F, Map[ChatId, GameDto]])(implicit F: Effect[F]) {

  def getGameSettings(chatId: ChatId): F[GameSettingsDto] = {
    ref.get.map(_.get(chatId).map(_.gameSettingsDto).getOrElse(GameSettingsDto(chatId)))
  }

  def updateGameSettings(chatId: ChatId, newGameSettingsDto: GameSettingsDto): F[Unit] = {
    ref.update {
      allGames =>
        val game = allGames.getOrElse(chatId, GameDto(GameSettingsDto(chatId))).copy(gameSettingsDto = newGameSettingsDto)
        allGames + (chatId -> game)
    }
  }

  def setQuestionsAmount(chatId: ChatId, amount: Int): F[Unit] = {
    for {
      gameSettings <- getGameSettings(chatId)
      newGameSettings = gameSettings.copy(questionsAmount = amount)
      _ <- updateGameSettings(chatId, newGameSettings)
    } yield ()
  }

  def setQuestionsCategory(chatId: ChatId, category: CategoryId): F[Unit] = {
    for {
      gameSettings <- getGameSettings(chatId)
      newGameSettings = gameSettings.copy(questionsCategory = category)
      _ <- updateGameSettings(chatId, newGameSettings)
    } yield ()
  }

  def getAllCategories: F[List[QuestionCategoryDto]] = {
    questionsDao.getAllCategories.map(toQuestionCategoryDtoList)
  }

  def initGame(chatId: ChatId): F[Unit] =
    for {
      gameSettings <- getGameSettings(chatId)
      questions <- questionsDao.generateRandomQuestions(gameSettings.questionsAmount, gameSettings.questionsCategory)
      _ <- ref.update {
        allGames =>
          val game = GameDto(gameSettings, convertQuestions(questions))
          allGames + (chatId -> game)
      }
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

  def getQuestionsInfo(chatId: ChatId): F[List[QuestionInfoDto]] = {
    ref.get.map(_.get(chatId).map(_.questions).orEmpty)
  }

  def getQuestionInfoById(chatId: ChatId, questionId: QuestionId): F[Option[QuestionInfoDto]] = {
    getQuestionsInfo(chatId).map(_.find(_.question.id == questionId))
  }

  def getRightAnswer(chatId: ChatId, questionId: QuestionId): F[Option[AnswerDto]] = {
    getQuestionInfoById(chatId, questionId).map(_.flatMap(_.question.answers.find(_.isRight)))
  }

  def getGameResult(chatId: ChatId): F[Option[List[GameResultDto]]] = {
    ref.get.map(_.get(chatId).map(game => {
      val userAnswersMap = game.questions.flatMap(_.userAnswers).groupBy(_.user.id)
      userAnswersMap.keys.flatMap(id => {
        val answers = userAnswersMap.get(id).orEmpty
        answers
          .map(x => GameResultDto(x.user.username.getOrElse(x.user.first_name), answers.count(_.isRight), game.questions.size))
      }).toList
    }))
  }

  def endGame(chatId: ChatId): F[Unit] = {
    ref.update(_ - chatId)
  }

  def setUserAnswer(chatId: ChatId, questionId: QuestionId, answer: AnswerDto): F[Unit] = {
    for {
      _ <- ref.update {
        allGames => {
          val questions = allGames.get(chatId).map(_.questions).orEmpty
          val currentQuestion = questions.find(_.question.id == questionId)
          val questionWithPreviousAnswers = currentQuestion.flatMap {
            q => q.userAnswers.find(_.user.id == answer.user.id)
              .map(a => q.copy(userAnswers = q.userAnswers.filterNot(_ == a)))
          }

          val newQuestion = questionWithPreviousAnswers match {
            case None => currentQuestion.map(q => q.copy(userAnswers = answer :: q.userAnswers))
            case Some(_) => questionWithPreviousAnswers.map(q => q.copy(userAnswers = answer :: q.userAnswers))
          }

          val newGame = allGames.get(chatId).map(x => x.copy(questions = {
            newQuestion.map(q => q :: x.questions.filterNot(_.question.id == questionId)).orEmpty
          })).getOrElse(GameDto(GameSettingsDto(chatId)))
          allGames + (chatId -> newGame)
        }
      }
    } yield ()
  }
}
