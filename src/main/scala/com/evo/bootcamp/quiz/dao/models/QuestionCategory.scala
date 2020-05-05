package com.evo.bootcamp.quiz.dao.models

import com.evo.bootcamp.quiz.dao.QuestionsDao.CategoryId

final case class QuestionCategory(id: CategoryId, name: String)
