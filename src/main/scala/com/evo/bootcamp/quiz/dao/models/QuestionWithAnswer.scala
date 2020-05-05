package com.evo.bootcamp.quiz.dao.models

import com.evo.bootcamp.quiz.dao.QuestionsDao.QuestionId

final case class QuestionWithAnswer(id: QuestionId, text: String, answer: Answer)
