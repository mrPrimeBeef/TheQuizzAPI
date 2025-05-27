package app.services;

import java.util.List;

import app.daos.GameDao;
import app.daos.PlayerDao;
import app.dtos.GameDTO;
import app.dtos.PlayerNameAndPoints;
import app.dtos.PlayerNamesDTO;
import app.entities.Game;
import app.entities.Player;
import app.entities.Question;

public class GameService {
    private final GameDao gameDao;
    private final PlayerDao playerDao;

    public GameService(GameDao gameDao, PlayerDao playerDao) {
        this.gameDao = gameDao;
        this.playerDao = playerDao;
    }

    public Game createGame(List<Player> players, List<Question> questions, Game activeGame) {
        activeGame.setPlayers(players);
        activeGame.setQuestions(questions);

        return gameDao.update(activeGame);
    }

    public List<Player> createPlayers(List<PlayerNameAndPoints> playerNamesAndPoints, Integer gameId) {
        return playerNamesAndPoints.stream()
                .map(playerNameAndPoints -> {
                    Player player = new Player();
                    player.setName(playerNameAndPoints.name());
                    player.setPoints(0);
                    player.setGame(gameDao.findById(gameId));
                    return playerDao.create(player);
                })
                .toList();
    }

    public Integer createNumberOfPlayers(int numberOfPlayers) {
        Game game = new Game();
        game.setNumberOfPlayers(numberOfPlayers);
        gameDao.create(game);
        return game.getId();
    }


    public PlayerNamesDTO getScores(Integer gameId) {
        List<Player> players = playerDao.findAllPlayersByGameId(gameId);
        return PlayerNamesDTO.convertFromEntityToDTO(players);
    }

    public GameDTO updateGame(Integer gameId, Integer turn, GameDTO updatedGame) {
        Game game = gameDao.findById(gameId);

        game.getPlayers().forEach(player -> {
            updatedGame.players().players().stream()
                    .filter(updatedPlayer -> updatedPlayer.name().equals(player.getName()))
                    .findFirst()
                    .ifPresent(updatedPlayer -> player.setPoints(updatedPlayer.points()));
        });

        game.setTurn(turn);

        gameDao.update(game);

        return new GameDTO(
                PlayerNamesDTO.convertFromEntityToDTO(game.getPlayers()),
                updatedGame.questions(),
                game.getTurn()
        );
    }
}