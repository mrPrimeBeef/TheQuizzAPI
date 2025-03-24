package app.entities;

import app.entities.enums.GameMode;
import jakarta.persistence.*;
import java.util.List;

@Entity
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToMany (mappedBy = "games")
    private List<User> users;

    @OneToMany
    private List<Player> players;

    @ManyToMany
    @JoinTable(
            name = "game_question",
            joinColumns = @JoinColumn(name = "game_id"),
            inverseJoinColumns = @JoinColumn(name = "question_id")
    )
    private List<Question> questions;
    @Enumerated(EnumType.STRING)
    private GameMode gameMode;

}
