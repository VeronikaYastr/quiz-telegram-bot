package com.evo.bootcamp.quiz.dao.models

import java.time.LocalDate
import java.util.UUID

final case class Author(id: UUID, name: String, birthday: LocalDate)
