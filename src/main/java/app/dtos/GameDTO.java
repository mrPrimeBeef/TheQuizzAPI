package app.dtos;

import java.util.List;

public record GameDTO(List<PlayerNamesDTO> players, List<QuestionDTO> questions) {
}
