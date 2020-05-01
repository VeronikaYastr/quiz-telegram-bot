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

class TelegramBotLogic[F[_]](questionsDao: QuestionsDao[F])(implicit F: Effect[F]) {

  val games: F[Ref[F, Map[ChatId, GameDto]]] = Ref[F].of(Map.empty[ChatId, GameDto])

  def gamesFromRef: F[Map[ChatId, GameDto]] = for {
    gameRef <- games
    game <- gameRef.get
  } yield game

  def getGameSettings(chatId: ChatId): F[GameSettingsDto] = {
    for {
      g <- gamesFromRef
    } yield g.get(chatId).map(_.gameSettingsDto).getOrElse(GameSettingsDto(chatId))
    //games.get(chatId).map(_.gameSettingsDto).getOrElse(GameSettingsDto(chatId))
  }

  def updateGameSettings(chatId: ChatId, newGameSettingsDto: GameSettingsDto): F[Unit] = {
    for {
      gameRef <- games
      _ <- gameRef.update {
        allGames =>
          val game = allGames.get(chatId).map(_.copy(gameSettingsDto = newGameSettingsDto))
          allGames + (chatId -> game.getOrElse(GameDto(GameSettingsDto(chatId))))
      }
    } yield ()
    /*games.flatMap(_.update {
      allGames =>
        val game = allGames.get(chatId).map(_.copy(gameSettingsDto = newGameSettingsDto))
        allGames + (chatId -> game.getOrElse(GameDto(GameSettingsDto(chatId))))
    })*/
    /* val newGame = games.get(chatId).map(_.copy(gameSettingsDto = newGameSettingsDto))
     games += (chatId -> newGame)*/
  }

  def setQuestionsAmount(chatId: ChatId, amount: Int): F[Unit] = {
    for {
      gameSettings <- getGameSettings(chatId)
      newGameSettings = gameSettings.copy(questionsAmount = amount)
      _ <- updateGameSettings(chatId, newGameSettings)
    } yield ()
    /* val newSettingsDto = getGameSettings(chatId).copy(questionsAmount = amount)
     updateGameSettings(chatId, newSettingsDto)*/
  }

  def setQuestionsCategory(chatId: ChatId, category: CategoryId): F[Unit] = {
    for {
      gameSettings <- getGameSettings(chatId)
      newGameSettings = gameSettings.copy(questionsCategory = category)
      _ <- updateGameSettings(chatId, newGameSettings)
    } yield ()
    /*val newSettingsDto = getGameSettings(chatId).copy(questionsCategory = category)
    updateGameSettings(chatId, newSettingsDto)*/
  }

  def getAllCategories: F[List[QuestionCategoryDto]] = {
    questionsDao.getAllCategories.map(toQuestionCategoryDtoList)
  }

  def initGame(chatId: ChatId): F[Unit] =
    for {
      gameSettings <- getGameSettings(chatId)
      questions <- questionsDao.generateRandomQuestions(gameSettings.questionsAmount, gameSettings.questionsCategory)
      _ = games.flatMap(_.update {
        allGames =>
          val game = GameDto(gameSettings, convertQuestions(questions))
          allGames + (chatId -> game)
      })
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
    gamesFromRef.map(_.get(chatId).map(_.questions).orEmpty)
  }

  def getQuestionInfoById(chatId: ChatId, questionId: QuestionId): F[Option[QuestionInfoDto]] = {
    getQuestionsInfo(chatId).map(_.find(_.question.id == questionId))
  }

  def getRightAnswer(chatId: ChatId, questionId: QuestionId): F[Option[AnswerDto]] = {
    getQuestionInfoById(chatId, questionId).map(_.flatMap(_.question.answers.find(_.isRight)))
  }

  def getGameResult(chatId: ChatId): F[Option[List[GameResultDto]]] = {
    gamesFromRef.map(_.get(chatId).map(game => {
      val userAnswersMap = game.questions.flatMap(_.userAnswers).groupBy(_.user.id)
      userAnswersMap.keys.flatMap(id => {
        val answers = userAnswersMap.get(id).orEmpty
        answers.map(x => GameResultDto(x.user.username.getOrElse(x.user.first_name), answers.count(_.isRight), game.questions.size))
      }).toList
    }))
  }

  def endGame(chatId: ChatId): F[Unit] = {
    games.flatMap(_.update(_ - chatId))
  }

  def setUserAnswer(chatId: ChatId, questionId: QuestionId, answer: AnswerDto): F[Unit] = {
    for {
      qInfo <- getQuestionInfoById(chatId, questionId)
      _ = qInfo.foreach(x => {
        x.userAnswers.find(_.user.id == answer.user.id).foreach(ans => {
          x.userAnswers = x.userAnswers.filterNot(_ == ans)
        })
        x.userAnswers ::= answer
      })
    } yield ()
  }
}
