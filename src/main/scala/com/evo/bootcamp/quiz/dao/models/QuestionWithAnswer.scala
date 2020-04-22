package com.evo.bootcamp.quiz.dao.models

final case class QuestionWithAnswer(id: Int, text: String, answerId: Int, answerText: String, answerIsRight: Boolean)
