package com.evo.bootcamp.quiz.dto

import com.evo.bootcamp.quiz.dao.QuestionsDao.CategoryId

final case class QuestionCategoryDto(id: CategoryId, name: String)
