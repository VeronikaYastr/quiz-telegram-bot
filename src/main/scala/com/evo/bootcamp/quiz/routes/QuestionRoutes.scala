package com.evo.bootcamp.quiz.routes

import cats.effect.IO
import cats.implicits._
import com.evo.bootcamp.quiz.dao.QuestionsDao
import com.evo.bootcamp.quiz.dao.models.Book
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.implicits._
import io.circe.generic.auto._
import org.http4s.circe.CirceEntityCodec._

object QuestionRoutes {

  def routes(questionsDao: QuestionsDao): HttpRoutes[IO] = HttpRoutes.of[IO]{
    case GET -> Root / "questions" => questionsDao.getQuestions.flatMap(q => Ok(q))

    case req@POST -> Root / "books" =>
      req.as[Book].flatMap(book => Ok(s"Hello ${book.title}!"))
  }

}
