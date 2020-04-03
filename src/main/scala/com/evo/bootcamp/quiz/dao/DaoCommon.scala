package com.evo.bootcamp.quiz.dao

import java.util.UUID

object DaoCommon {
  protected final val authorId1 = UUID.randomUUID()
  protected final val authorId2 = UUID.randomUUID()
  protected final val bookId1 = UUID.randomUUID()
  protected final val bookId2 = UUID.randomUUID()
  protected final val bookId3 = UUID.randomUUID()

  final val authorsSql =
    """CREATE TABLE authors (
      |  id UUID PRIMARY KEY,
      |  name VARCHAR(100) NOT NULL,
      |  birthday DATE);""".stripMargin
  final val booksSql =
    """CREATE TABLE books (
      |  id UUID PRIMARY KEY,
      |  author UUID NOT NULL,
      |  title VARCHAR(100) NOT NULL,
      |  year INT,
      |  genre VARCHAR(100) NOT NULL,
      |  FOREIGN KEY (author) REFERENCES authors(id));""".stripMargin
  final val populateDataSql =
    s"""
       |INSERT INTO authors (id, name, birthday) VALUES
       |  ('$authorId1', 'Martin Odersky', '1958-09-05'),
       |  ('$authorId2', 'J.K. Rowling', '1965-07-31');
       |
       |INSERT INTO books (id, author, title, year, genre) VALUES
       |  ('$bookId1', '$authorId1', 'Programming in Scala', 2016, 'Computer science'),
       |  ('$bookId2', '$authorId2', 'Harry Potter and Philosopher''s Stone', 1997, 'Fantasy'),
       |  ('$bookId3', '$authorId2', 'Harry Potter and the Chamber of Secrets', 1998, 'Fantasy');
       |""".stripMargin

  val fetchBooksCommonSql: String =
    """SELECT b.id, a.id, a.name, a.birthday, b.title, b.year, b.genre FROM books b
      |INNER JOIN authors a ON b.author = a.id """.stripMargin
  val fetchHPBooksSql: String = fetchBooksCommonSql + s"WHERE b.author = '$authorId2';"

}
