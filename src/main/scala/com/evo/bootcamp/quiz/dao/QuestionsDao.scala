package com.evo.bootcamp.quiz.dao

import cats.data.NonEmptyList
import cats.effect.IO._
import cats.effect._
import com.evo.bootcamp.quiz.dao.models.{LikeInfo, QuestionWithAnswer}
import com.evo.bootcamp.quiz.dto.{Answer, Question}
import doobie._
import doobie.util.transactor.Transactor
import doobie.implicits._
import doobie.implicits.javatime._
import doobie.postgres._

class QuestionsDao[F[_]](xa: Transactor[F])(implicit F: Effect[F]) {

  def initGame(userId: Long, amount: Int): F[Int] = {
    val initGame = sql"insert into game (userId, amount) values ($userId, $amount) RETURNING id;"
    initGame.query[Int].unique.transact(xa)
  }

  def generateRandomQuestions(amount: Int): F[List[QuestionWithAnswer]] = {
    val queryQuestions = sql"select q.id, q.text, a.id, a.text, a.isRight from (select * from questions order by random() limit $amount) q inner join answers a on q.id = a.questionid"
    queryQuestions.queryWithLogHandler[(Int, String, Int, String, Boolean)](LogHandler.jdkLogHandler).map{case (qId, qt, aId, at, isR) => QuestionWithAnswer(qId, qt, aId, at, isR)}.to[List].transact(xa)
  }

  def saveQuestionForUser(questionId: Int, userId: Long, gameId: Int): F[Int] = {
    val saveQuestion = sql"insert into game_process (gameId, userId, questionId) values ($gameId, $userId, $questionId);"
    saveQuestion.update.run.transact(xa)
  }

  def getQuestionId(userId: Long, gameId: Long): F[Int] = {
    val getQuestionId = sql"select questionId from game_process where id=(select min(id) from game_process where gameId=$gameId and userId=$userId and answerId is null)"
    getQuestionId.query[Int].unique.transact(xa)
  }

  def setQuestionLikeInfo(questionId: Int, userLike: Boolean, likeInfo: LikeInfo): F[Int] = {
    if (userLike) likeInfo.likesCount += 1 else likeInfo.dislikesCount += 1
    val setQuestionLikeInfo = sql"update questions set likesCount = ${likeInfo.likesCount}, dislikesCount = ${likeInfo.dislikesCount} where id = $questionId"
    setQuestionLikeInfo.update.run.transact(xa)
  }

  def getQuestionsLikeInfo(questionId: Int): F[LikeInfo] = {
    val getQuestionLikeInfo = sql"select likesCount, dislikesCount from questions where id = $questionId"
    getQuestionLikeInfo.queryWithLogHandler[(Int, Int)](LogHandler.jdkLogHandler).map{case (lCount, dCount) => LikeInfo(lCount, dCount)}.unique.transact(xa)
  }

  def getGameId(userId: Long): F[Int] = {
    val getGameId = sql"select id from game where userId=$userId and create_date=(select max(create_date) from game where userId=$userId)"
    getGameId.query[Int].unique.transact(xa)
  }

  def getQuestionsAmount: F[Int] = {
    val queryQuestionsAmount = sql"select count(*) from questions";
    queryQuestionsAmount.query[Int].unique.transact(xa)
  }
}
