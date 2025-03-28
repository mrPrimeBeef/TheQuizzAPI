package app.entities;

import java.util.List;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import app.entities.enums.Difficulty;

@NoArgsConstructor
@Getter
@Entity
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Setter
    private String description;

    @Setter
    private String rightAnswer;

    @Setter
    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> wrongAnswers;

    private String category;

    @Enumerated(EnumType.STRING)
    private Difficulty difficulty;

    @ManyToMany(mappedBy = "questions")
    private List<Game> games;

    public Question(String description, String rightAnswer, List<String> wrongAnswers, String category, Difficulty difficulty) {
        this.description = description;
        this.rightAnswer = rightAnswer;
        this.wrongAnswers = wrongAnswers;
        this.category = category;
        this.difficulty = difficulty;
    }
}