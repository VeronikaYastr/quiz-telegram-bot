package com.evo.bootcamp.quiz.dto

import com.evo.bootcamp.quiz.TelegramBotCommand.ChatId
import com.evo.bootcamp.quiz.dao.QuestionsDao.CategoryId
import com.evo.bootcamp.quiz.dto.GameDto.GameSettingsDto

case class GameDto(gameSettingsDto: GameSettingsDto,
                         questions: List[QuestionInfoDto] = List.empty)

object GameDto {
  final case class GameSettingsDto(chatId: ChatId, questionsAmount: Int = 10, questionsCategory: CategoryId = 0)
}