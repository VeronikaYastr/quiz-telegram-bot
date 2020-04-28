package com.evo.bootcamp.quiz

import cats.effect.{Effect, IO, LiftIO}
import com.evo.bootcamp.quiz.dao.QuestionsDao
import com.evo.bootcamp.quiz.dao.models.QuestionCategory
import com.evo.bootcamp.quiz.dto.QuestionCategoryDto
import org.mockito.MockitoSugar
import org.scalatest._
import org.scalatest.flatspec.{AnyFlatSpec, AsyncFlatSpec}
import org.scalatest.matchers.should.Matchers
import cats.effect.testing.specs2.CatsIO
import org.specs2.mutable.Specification

import scala.concurrent.Future

class LogicTest extends Specification with CatsIO with MockitoSugar {


  "Logic" >> {
    "extract all categories" >> {
      val questionDaoMock = mock[QuestionsDao[IO]]
      when(questionDaoMock.getAllCategories).thenReturn(
        IO { List(QuestionCategory(1, "History"), QuestionCategory(2, "Science")) }
      )

      val logicService = new TelegramBotLogic[IO](questionDaoMock)
      val res = logicService.getAllCategories
      res.map(
        _ must contain(QuestionCategoryDto(1, "History"), QuestionCategoryDto(2, "Science"))
      )
    }
  }
}
