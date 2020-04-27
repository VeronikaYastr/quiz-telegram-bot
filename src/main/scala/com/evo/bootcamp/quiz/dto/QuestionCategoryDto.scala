package com.evo.bootcamp.quiz.dto

import com.evo.bootcamp.quiz.dao.QuestionsDao.CategoryId
import com.evo.bootcamp.quiz.utils.MessageTexts._

final case class QuestionCategoryDto(id: CategoryId = 0, name: String = `allCategoryText`)
