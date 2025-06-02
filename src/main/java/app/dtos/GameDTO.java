package app.dtos;

import app.entities.enums.GameMode;

public record GameDTO(PlayerNamesDTO players, QuestionDTO questions, int turn, GameMode gameMode) {
}
