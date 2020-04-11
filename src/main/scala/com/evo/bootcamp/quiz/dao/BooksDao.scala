package com.evo.bootcamp.quiz.dao

import java.time.Year
import java.util.UUID

import cats.effect.IO._
import cats.effect._
import com.evo.bootcamp.quiz.dao.models.{Book, Question}
import doobie._
import doobie.util.transactor.Transactor
import doobie.implicits._
import doobie.implicits.javatime._
import doobie.h2._

trait QuestionsDao {
  def getQuestions: IO[List[Question]]
}

class QuestionsDaoImpl(xa: Transactor[IO]) extends QuestionsDao {
  implicit val uuidMeta: Meta[UUID] = Meta[String].timap(UUID.fromString)(_.toString)
  implicit val yearMeta: Meta[Year] = Meta[Int].timap(Year.of)(_.getValue)

  override def getQuestions: IO[List[Question]] = {
    val queryQuestions = sql"select id, rightAnswer, text, category, likesCount, disLikesCount from questions;";
    queryQuestions.queryWithLogHandler[Question](LogHandler.jdkLogHandler).to[List].transact(xa)
  }
}
