package app.dtos;

import java.util.List;

public record QuestionBody(
        String type,
        String difficulty,
        String category,
        String question,
        String correct_answer,
        List<String> incorrect_answers
) {

}