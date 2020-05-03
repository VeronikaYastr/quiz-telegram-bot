package com.evo.bootcamp.quiz.dao

import cats.effect._
import com.evo.bootcamp.quiz.dao.QuestionsDao.CategoryId
import com.evo.bootcamp.quiz.dao.models.{Answer, QuestionCategory, QuestionWithAnswer}
import doobie._
import doobie.util.transactor.Transactor
import doobie.implicits._
import doobie.implicits.javatime._
import doobie.postgres._

class QuestionsDao[F[_]](xa: Transactor[F])(implicit F: Effect[F]) {

  def generateRandomQuestions(amount: Int, category: CategoryId): F[List[QuestionWithAnswer]] = {
    val categoryFragment = List(s"where category = $category")
    val questionsQuery = List("select * from questions")
    val amountQuery = List(s"order by random() limit $amount")
    val queryWithCategory = if (category != 0) questionsQuery ::: categoryFragment else questionsQuery
    val fullQuery = queryWithCategory ::: amountQuery

    val questionsWithAnswersQuery = sql"select q.id, q.text, a.id, a.text, a.isRight from (" ++ Fragment.const(fullQuery.mkString(" ")) ++ sql") q inner join answers a on q.id = a.questionid"
    questionsWithAnswersQuery.queryWithLogHandler[(Int, String, Int, String, Boolean)](LogHandler.jdkLogHandler).map{case (qId, qt, aId, at, isR) => QuestionWithAnswer(qId, qt, Answer(aId, at, isR))}.to[List].transact(xa)
  }

  def getAllCategories: F[List[QuestionCategory]] = {
    val categoryQuery = sql"select * from category"
    categoryQuery.queryWithLogHandler[QuestionCategory](LogHandler.jdkLogHandler).to[List].transact(xa)
  }

}

object QuestionsDao {
  type AnswerId = Int
  type QuestionId = Int
  type CategoryId = Int
}