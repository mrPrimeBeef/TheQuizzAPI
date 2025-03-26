package app.controller;

import app.config.HibernateConfig;
import app.daos.PlayerDao;
import app.daos.QuestionDao;
import app.dtos.*;
import app.entities.Game;
import app.entities.Player;
import app.entities.Question;
import app.exceptions.ValidationException;
import app.services.GameService;
import app.utils.Populate;
import io.javalin.http.Context;
import jakarta.persistence.EntityManagerFactory;

import java.util.List;
import java.util.stream.Collectors;

public class GameController {
    EntityManagerFactory emf;
    private final GameService gameService;
    private QuestionDao questionDao = QuestionDao.getInstance(emf);
    private PlayerDao playerDao = PlayerDao.getInstance(emf);

    public GameController(GameService gameService, EntityManagerFactory emf) {
        this.gameService = gameService;
    }

    public GameDTO makeGame(Context ctx) {
        try {
            String limitStr = ctx.queryParam("limit");

            List<Question> questions = questionDao.findAll();

            List<String> uniqueCategories = questions.stream()
                    .map(Question::getCategory)
                    .distinct()
                    .collect(Collectors.toList());

            String category = ctx.queryParam("category");
            if (category != null && !uniqueCategories.contains(category)) {
                throw new ValidationException("From makeGame(), cannot make a game with the category " + category);
            }

            String difficulty = ctx.queryParam("difficulty");
            if (difficulty != null &&
                    (!difficulty.equals("EASY") &&
                            !difficulty.equals("MEDIUM") &&
                            !difficulty.equals("HARD"))) {
                throw new ValidationException("From makeGame(), cannot make a game with the difficulty " + difficulty);
            }

            int limit = limitStr != null ? Integer.parseInt(limitStr) : 10;
            if (limit < 0 || limit > 50) {
                throw new ValidationException("From makeGame(), cannot make a game with the question size " + limit + ". Atleast 10 questions and maxium 50");
            }

            List<Question> filteredQuestions = questions.stream()
                    .filter(question -> question.getCategory().equals(category))
                    .filter(question -> question.getDifficulty().name().equals(difficulty))
                    .limit(limit)
                    .collect(Collectors.toList());

            List<Player> players = playerDao.findAll();

            Game game = gameService.createGame(players, filteredQuestions);

            List<PlayerNameAndPoints> playerNameAndPointsList = players.stream()
                    .map(player -> new PlayerNameAndPoints(player.getName(), player.getPoints()))
                    .toList();

            PlayerNamesDTO playerNamesDTO = new PlayerNamesDTO(playerNameAndPointsList);

            List<QuestionBody> questionBodyList = filteredQuestions.stream()
                    .map(q -> new QuestionBody(q.getDifficulty().toString(),q.getCategory(), q.getDescription(),q.getRightAnswer(),q.getWrongAnswers()))
                    .toList();
            QuestionDTO questionDTO = new QuestionDTO(questionBodyList);

            return new GameDTO(playerNamesDTO,questionDTO);

        } catch (Exception e) {

        }
        return null;
    }

    public Integer getNumberOfPlayers(Context ctx) {
        String numberOfPlayers = ctx.pathParam("number");
        return Integer.parseInt(numberOfPlayers);
    }

    public List<Player> createPlayers(Context ctx) {
        PlayerNamesDTO playerNamesDTO = ctx.bodyAsClass(PlayerNamesDTO.class);
        return gameService.createPlayers(playerNamesDTO.players());
    }

    public QuestionBody getOneQuestion() {
        Question q = questionDao.findById(1);
        return new QuestionBody(q.getDifficulty().toString(),q.getCategory(),q.getDescription(),q.getRightAnswer(),q.getWrongAnswers());
    }

    public void populateDatabase(Context ctx) {
        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
        Populate.questionAndUserData(emf);
    }
}
