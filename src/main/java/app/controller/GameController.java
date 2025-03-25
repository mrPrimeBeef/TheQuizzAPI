package app.controller;

import app.daos.QuestionDao;
import app.dtos.PlayerNamesDTO;
import app.entities.Player;
import app.entities.Question;
import app.exceptions.ValidationException;
import app.services.GameService;
import io.javalin.http.Context;

import java.util.List;
import java.util.stream.Collectors;

public class GameController {
    private final GameService gameService;
    private QuestionDao questionDao = QuestionDao.getInstance();

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    public void makeGame(Context ctx) {
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
                throw new ValidationException("From makeGame(), cannot make a game with the question size " + limit);
            }

            List<Question> filteredQuestions = questions.stream()
                    .filter(question -> question.getCategory().equals(category))
                    .filter(question -> question.getDifficulty().name().equals(difficulty))
                    .limit(limit)
                    .collect(Collectors.toList());

        } catch (Exception e) {

        }
    }

    public Integer getNumberOfPlayers(Context ctx) {
        String numberOfPlayers = ctx.pathParam("number");
        return Integer.parseInt(numberOfPlayers);
    }

    public List<Player> createPlayers(Context ctx) {
        PlayerNamesDTO playerNamesDTO = ctx.bodyAsClass(PlayerNamesDTO.class);
        return gameService.createPlayers(playerNamesDTO.players());
    }
}
