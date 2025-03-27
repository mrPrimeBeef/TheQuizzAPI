package app.services;

import app.daos.GameDao;
import app.daos.PlayerDao;
import app.dtos.PlayerNameAndPoints;
import app.entities.Game;
import app.entities.Player;
import app.entities.Question;

import java.util.List;

public class GameService {
    private final GameDao gameDao;
    private final PlayerDao playerDao;

    public GameService(GameDao gameDao, PlayerDao playerDao) {
        this.gameDao = gameDao;
        this.playerDao = playerDao;
    }

    public Game createGame(List<Player> players, List<Question> questions) {
        Game game = new Game();
        game.setPlayers(players);
        game.setQuestions(questions);
//        game.s
        return gameDao.create(game);
    }

    public List<Player> createPlayers(List<PlayerNameAndPoints> playerNamesAndPoints) {
        return playerNamesAndPoints.stream()
                .map(playerNameAndPoints -> {
                    Player player = new Player();
                    player.setName(playerNameAndPoints.name());
                    player.setPoints(playerNameAndPoints.points());
                    return playerDao.create(player);
                })
                .toList();
    }
}