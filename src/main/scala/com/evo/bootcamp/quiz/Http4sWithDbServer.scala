package com.evo.bootcamp.quiz

import com.evo.bootcamp.quiz.dao.BooksDao.BooksDaoImpl
import com.evo.bootcamp.quiz.routes.BooksRoutes
import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import com.evo.bootcamp.quiz.config.{Config, ServerConfig}
import com.evo.bootcamp.quiz.dao.DaoInit
import doobie.util.transactor.Transactor
import fs2.Stream
import org.http4s._
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder

object Http4sWithDbServer extends IOApp {

  def makeRoutes(xa: Transactor[IO]): HttpRoutes[IO] = BooksRoutes.routes(new BooksDaoImpl(xa)) /*<+> jsonRoutes*/

  def serveStream(transactor: Transactor[IO], serverConfig: ServerConfig): Stream[IO, ExitCode] = {
    BlazeServerBuilder[IO]
      .bindHttp(serverConfig.port, serverConfig.host)
      .withHttpApp(makeRoutes(transactor).orNotFound)
      .serve
  }

  override def run(args: List[String]): IO[ExitCode] = {

    val transactor = for {
      config <- Config.load()
      tr <- IO(DaoInit.transactor(config.dbConfig))
      _ <- DaoInit.initTables(tr)
    } yield tr

    val stream = for {
      tr <- Stream.eval(transactor)
      serv <- serveStream(tr, ServerConfig("localhost", 9000))
    } yield serv

    stream.compile.drain.as(ExitCode.Success)
  }
}
