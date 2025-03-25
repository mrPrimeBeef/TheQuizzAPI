package app.dtos;

import java.util.List;

public record QuestionResponseDTO(int response_code, List<QuestionResponseBody> results) {}
