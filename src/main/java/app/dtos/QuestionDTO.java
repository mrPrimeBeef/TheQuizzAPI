package app.dtos;

import app.entities.Question;

import java.util.List;

public record QuestionDTO(List<QuestionBody> results) {

    public static QuestionDTO convertFromEntityToDTO(List<Question> questions) {
        List<QuestionBody> list = questions.stream()
                .map(question -> new QuestionBody(
                        question.getDifficulty().toString(),
                        question.getCategory(),
                        question.getDescription(),
                        question.getRightAnswer(),
                        question.getWrongAnswers()
                ))
                .toList();

        return new QuestionDTO(list);
    }
}
