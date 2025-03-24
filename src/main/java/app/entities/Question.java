package app.entities;

import app.entities.enums.Difficulty;
import jakarta.persistence.*;
import lombok.Getter;


import java.util.List;

@Getter
@Entity
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String description;
    private String rightAnswer;
    @ElementCollection
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