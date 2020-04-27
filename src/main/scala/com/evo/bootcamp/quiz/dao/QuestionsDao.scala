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
    val categoryFragment = fr"where category = $category"
    var questionsQuery = fr"select * from questions"
    val amountQuery = fr"order by random() limit $amount"
    if (category != 0) questionsQuery ++= categoryFragment
    questionsQuery ++= amountQuery

    val questionsWithAnswersQuery = fr"select q.id, q.text, a.id, a.text, a.isRight from (" ++ questionsQuery ++ fr") q inner join answers a on q.id = a.questionid"
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