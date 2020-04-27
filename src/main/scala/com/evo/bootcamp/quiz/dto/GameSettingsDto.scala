package com.evo.bootcamp.quiz.dto

import com.evo.bootcamp.quiz.TelegramBotCommand.ChatId
import com.evo.bootcamp.quiz.dao.QuestionsDao.CategoryId

final case class GameSettingsDto(chatId: ChatId, var questionsAmount: Int = 10, var questionsCategory: CategoryId = 0)