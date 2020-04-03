package com.evo.bootcamp.quiz.dao.dto

import java.time.LocalDate
import java.util.UUID

final case class Author(id: UUID, name: String, birthday: LocalDate)
