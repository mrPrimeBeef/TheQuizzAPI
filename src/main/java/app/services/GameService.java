package app.services;

import java.util.List;

import app.daos.GameDao;
import app.daos.PlayerDao;
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
                    player.setPoints(playerNameAndPoints.points());
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

    public void updateScore(Player player, Question question, String answer){
        int playerpoints;

        if (answer.equals(question.getRightAnswer())) {
            switch (question.getDifficulty()) {
                case EASY:
                    playerpoints = player.getPoints() + 10;
                    player.setPoints(playerpoints);
                    playerDao.update(player);
                    break;
                case MEDIUM:
                    playerpoints = player.getPoints() + 15;
                    player.setPoints(playerpoints);
                    playerDao.update(player);
                    break;
                case HARD:
                    playerpoints = player.getPoints() + 20;
                    player.setPoints(playerpoints);
                    playerDao.update(player);
                    break;
                default:
                    // Add logic for default case here
                    break;
            }
        }
    }

    public PlayerNamesDTO getScores(Integer gameId) {
        List<Player> players = playerDao.findAllPlayersByGameId(gameId);
        return PlayerNamesDTO.convertFromEntityToDTO(players);
    }
}