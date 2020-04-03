package com.evo.bootcamp.quiz.dao

import java.time.Year
import java.util.UUID

import cats.effect.IO._
import cats.effect._
import com.evo.bootcamp.quiz.dao.dto.Book
import doobie._
import doobie.implicits._
import doobie.implicits.javatime._
import doobie.h2._
import doobie.util.transactor.Transactor

trait BooksDao {
  def getBooks: IO[List[Book]]
}

object BooksDao {

  class BooksDaoImpl(xa: Transactor[IO]) extends BooksDao {
    implicit val uuidMeta: Meta[UUID] = Meta[String].timap(UUID.fromString)(_.toString)
    implicit val yearMeta: Meta[Year] = Meta[Int].timap(Year.of)(_.getValue)

    override def getBooks: IO[List[Book]] = {
      val queryHPBooks = Fragment.const(DaoCommon.fetchBooksCommonSql)
      queryHPBooks.queryWithLogHandler[Book](LogHandler.jdkLogHandler).to[List].transact(xa)
    }
  }

}
