package app.entities;

import java.util.ArrayList;
import java.util.List;

import app.entities.enums.GameMode;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToMany(mappedBy = "games")
    private List<User> users = new ArrayList<>();

    @OneToMany(mappedBy = "game", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Player> players;

    private Integer numberOfPlayers;

    private Integer turn;

    @ManyToMany
    @JoinTable(
            name = "game_question",
            joinColumns = @JoinColumn(name = "game_id"),
            inverseJoinColumns = @JoinColumn(name = "question_id")
    )
    private List<Question> questions;

    @Enumerated(EnumType.STRING)
    private GameMode gameMode;

    public Game(List<Player> players, List<Question> questions, Integer numberOfPlayers) {
        this.players = players;
        this.questions = questions;
        this.numberOfPlayers = numberOfPlayers;
        this.gameMode = gameMode;
    }

    public void addPlayer(Player player) {
        player.setGame(this);
    }
}