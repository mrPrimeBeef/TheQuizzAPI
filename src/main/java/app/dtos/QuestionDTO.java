package app.dtos;

import java.util.List;

public record QuestionDTO(int response_code, List<QuestionBody> results) {}
