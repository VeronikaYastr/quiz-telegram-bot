package com.evo.bootcamp.quiz

import cats.effect.Effect
import com.evo.bootcamp.quiz.dao.QuestionsDao
import com.evo.bootcamp.quiz.dao.models.QuestionCategory
import org.mockito.MockitoSugar
import org.scalatest.BeforeAndAfter
import org.scalatest.funsuite.AnyFunSuite

class LogicTest[F[_]](implicit F: Effect[F]) extends AnyFunSuite with BeforeAndAfter with MockitoSugar {
  test ("test logic") {
    val dao = mock[QuestionsDao[F]]
    val logicService = new TelegramBotLogic[F](dao)
    when(dao.getAllCategories).thenReturn(F.pure(List(QuestionCategory(1, "History"))))

    val allCategories = logicService.getAllCategories
    verify(dao.getAllCategories)
  }
}
