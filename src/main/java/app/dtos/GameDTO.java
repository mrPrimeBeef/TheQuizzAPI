package app.dtos;

import app.entities.enums.GameMode;

public record GameDTO(Integer gameId, PlayerNamesDTO players, QuestionDTO questions, Integer turn, GameMode gameMode) {
}
