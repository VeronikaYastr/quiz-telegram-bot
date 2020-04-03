package com.evo.bootcamp.quiz.dao.dto

import java.time.Year
import java.util.UUID

final case class Book(id: UUID, author: Author, title: String, year: Year, genre: String) {
  override def toString: String = s"$title ($year) by ${author.name} with genre $genre"
}
