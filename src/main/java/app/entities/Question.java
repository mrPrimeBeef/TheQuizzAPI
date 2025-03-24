package app.entities;

import app.entities.enums.Difficulty;
import app.entities.enums.GameMode;
import jakarta.persistence.*;
import java.util.List;

@Entity
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String description;
    private String rightAnswer;
    private String wrongAnswers;
    private String category;

    @Enumerated(EnumType.STRING)
    private Difficulty difficulty;

    @Enumerated(EnumType.STRING)
    private GameMode gameMode;

    @ManyToMany(mappedBy = "questions")
    private List<Game> games;
}
