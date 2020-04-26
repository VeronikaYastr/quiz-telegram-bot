package com.evo.bootcamp.quiz.dao

import cats.effect._
import com.evo.bootcamp.quiz.dao.QuestionsDao.QuestionId
import com.evo.bootcamp.quiz.dao.models.{Answer, LikeInfo, QuestionWithAnswer}
import doobie._
import doobie.util.transactor.Transactor
import doobie.implicits._
import doobie.implicits.javatime._
import doobie.postgres._

class QuestionsDao[F[_]](xa: Transactor[F])(implicit F: Effect[F]) {

  def generateRandomQuestions(amount: Int): F[List[QuestionWithAnswer]] = {
    val queryQuestions = sql"select q.id, q.text, a.id, a.text, a.isRight from (select * from questions order by random() limit $amount) q inner join answers a on q.id = a.questionid"
    queryQuestions.queryWithLogHandler[(Int, String, Int, String, Boolean)](LogHandler.jdkLogHandler).map{case (qId, qt, aId, at, isR) => QuestionWithAnswer(qId, qt, Answer(aId, at, isR))}.to[List].transact(xa)
  }

  def setQuestionLikeInfo(questionId: QuestionId, userLike: Boolean, likeInfo: LikeInfo): F[Int] = {
    if (userLike) likeInfo.likesCount += 1 else likeInfo.dislikesCount += 1
    val setQuestionLikeInfo = sql"update questions set likesCount = ${likeInfo.likesCount}, dislikesCount = ${likeInfo.dislikesCount} where id = $questionId"
    setQuestionLikeInfo.update.run.transact(xa)
  }

  def getQuestionsLikeInfo(questionId: QuestionId): F[LikeInfo] = {
    val getQuestionLikeInfo = sql"select likesCount, dislikesCount from questions where id = $questionId"
    getQuestionLikeInfo.queryWithLogHandler[(Int, Int)](LogHandler.jdkLogHandler).map{case (lCount, dCount) => LikeInfo(lCount, dCount)}.unique.transact(xa)
  }

}

object QuestionsDao {
  type AnswerId = Int
  type QuestionId = Int
}