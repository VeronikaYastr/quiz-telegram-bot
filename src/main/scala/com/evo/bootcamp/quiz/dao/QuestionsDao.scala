package com.evo.bootcamp.quiz.dao

import cats.data.NonEmptyList
import cats.effect.IO._
import cats.effect._
import com.evo.bootcamp.quiz.dao.models.Question
import doobie._
import doobie.util.transactor.Transactor
import doobie.implicits._
import doobie.implicits.javatime._
import doobie.postgres._

class QuestionsDao[F[_]](xa: Transactor[F])(implicit F: Effect[F]) {

  def getQuestions: F[List[Question]] = {
    val queryQuestions = sql"select id, rightAnswer, text, category, likesCount, disLikesCount from questions;";
    queryQuestions.queryWithLogHandler[Question](LogHandler.jdkLogHandler).to[List].transact(xa)
  }

  def initGame(userId: Long, amount: Int): F[Int] = {
    val initGame = sql"insert into game (userId, amount) values ($userId, $amount) RETURNING id;"
    initGame.query[Int].unique.transact(xa)
  }

  def generateRandomQuestions(amount: Int): F[List[Question]] = {
    val queryQuestions = sql"select * from questions order by random() limit $amount "
    queryQuestions.queryWithLogHandler[Question](LogHandler.jdkLogHandler).to[List].transact(xa)
  }

  def saveQuestionForUser(questionId: Int, userId: Long, gameId: Int): F[Int] = {
    val saveQuestion = sql"insert into game_process (gameId, userId, questionId) values ($gameId, $userId, $questionId);"
    saveQuestion.update.run.transact(xa)
  }

  def getQuestionId(userId: Long, gameId: Long): F[Int] = {
    val getQuestionId = sql"select questionId from game_process where id=(select min(id) from game_process where gameId=$gameId and userId=$userId and answerId is null)"
    getQuestionId.query[Int].unique.transact(xa)
  }

  def getQuestion(questionId: Int): F[Question] = {
    val getQuestion = sql"select * from questions where id=$questionId;";
    getQuestion.query[Question].unique.transact(xa)
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
