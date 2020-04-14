package com.evo.bootcamp.quiz.dao

import java.time.Year
import java.util.UUID

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
  implicit val uuidMeta: Meta[UUID] = Meta[String].timap(UUID.fromString)(_.toString)
  implicit val yearMeta: Meta[Year] = Meta[Int].timap(Year.of)(_.getValue)

  def getQuestions: F[List[Question]] = {
    val queryQuestions = sql"select id, rightAnswer, text, category, likesCount, disLikesCount from questions;";
    queryQuestions.queryWithLogHandler[Question](LogHandler.jdkLogHandler).to[List].transact(xa)
  }

  def generateRandomQuestions(amount: Int): F[List[Question]] = {
    val queryQuestions = sql"select * from questions order by random() limit $amount "
    queryQuestions.queryWithLogHandler[Question](LogHandler.jdkLogHandler).to[List].transact(xa)
  }

  def getQuestionsAmount: F[Int] = {
    val queryQuestionsAmount = sql"select count(*) from questions";
    queryQuestionsAmount.query[Int].unique.transact(xa)
  }
}
