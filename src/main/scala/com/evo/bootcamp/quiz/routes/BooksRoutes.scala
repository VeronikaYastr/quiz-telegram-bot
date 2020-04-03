package com.evo.bootcamp.quiz.routes

import cats.effect.IO
import cats.implicits._
import com.evo.bootcamp.quiz.dao.BooksDao
import com.evo.bootcamp.quiz.dao.dto.Book
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.implicits._
import io.circe.generic.auto._
import org.http4s.circe.CirceEntityCodec._

object BooksRoutes {

  def routes(booksDao: BooksDao): HttpRoutes[IO] = HttpRoutes.of[IO]{
    case GET -> Root / "books" => booksDao.getBooks.flatMap(b => Ok(b))

    case req@POST -> Root / "books" =>
      req.as[Book].flatMap(book => Ok(s"Hello ${book.title}!"))
  }

}
