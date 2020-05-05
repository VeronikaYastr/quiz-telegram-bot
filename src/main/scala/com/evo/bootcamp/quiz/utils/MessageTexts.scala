package com.evo.bootcamp.quiz.utils

import com.evo.bootcamp.quiz.TelegramBotCommand.{help, start, stop}

object MessageTexts {
  val startGameMessage = "Ваша игра началась. \nУ вас есть *15 секунд* на ответ ⏳"
  val endGameMessage = s"Игра закончена 🥳 "
  val rightAnswerText: String => String = rightAnswer => s"_Правильный ответ_: $rightAnswer"
  val allCategoryText = "❓Все"
  val questionsAmountMessage = "\uD83E\uDDE9 Выберите количество вопросов"
  val questionsCategoryMessage = "\uD83D\uDC68\u200D\uD83C\uDF93 Выберите категорию вопросов"
  val helpMessage: String =  List(
    "С помощью этого бота можно играть в квиз. Команды:",
    s"`$help` - просмотр этого сообщения",
    s"`$start`- начать игру",
    s"`$stop` - остановить игру"
  ).mkString("\n")
}