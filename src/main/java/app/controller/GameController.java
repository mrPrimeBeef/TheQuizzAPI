package app.controller;

import java.util.List;


import app.utils.Utils;
import io.javalin.http.Context;
import jakarta.persistence.EntityManagerFactory;

import app.daos.*;
import app.dtos.*;
import app.entities.Game;
import app.entities.Player;
import app.entities.Question;
import app.exceptions.ValidationException;
import app.services.GameService;
import org.jetbrains.annotations.NotNull;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;


public class GameController {
    private final GameService gameService;
    private QuestionDao questionDao;
    private PlayerDao playerDao;
    private GameDao gameDao;
    private EntityManagerFactory emf;

    public GameController(GameService gameService, EntityManagerFactory emf) {
        this.gameService = gameService;
        this.questionDao = QuestionDao.getInstance(emf);
        this.playerDao = PlayerDao.getInstance(emf);
        this.gameDao = GameDao.getInstance(emf);
    }

    public GameDTO makeGame(Context ctx) throws ValidationException {
        GameRequestDTO gameRequest = ctx.bodyAsClass(GameRequestDTO.class);
        try {
            List<Question> filteredQuestions = validatInputAndReturnFilteredQuestions(gameRequest);

            Integer gameid = Integer.parseInt(ctx.pathParam("gameid"));
            List<Player> players = playerDao.findAllPlayersByGameId(gameid);
            Game activeGame = gameDao.findById(gameid);

            gameService.createGame(players, filteredQuestions, activeGame);

            return getGameDTO(players, filteredQuestions);
        } catch (Exception e) {
            throw new ValidationException("Error creating game: " + e.getMessage());
        }
    }

    private static @NotNull GameDTO getGameDTO(List<Player> players, List<Question> filteredQuestions) {
        List<PlayerNameAndPoints> playerNameAndPointsList = players.stream()
                .map(player -> new PlayerNameAndPoints(player.getName(), player.getPoints()))
                .toList();

        PlayerNamesDTO playerNamesDTO = new PlayerNamesDTO(playerNameAndPointsList);

        List<QuestionBody> questionBodyList = filteredQuestions.stream()
                .map(q -> new QuestionBody(q.getDifficulty().toString(), q.getCategory(), q.getDescription(), q.getRightAnswer(), q.getWrongAnswers()))
                .toList();
        QuestionDTO questionDTO = new QuestionDTO(questionBodyList);

        return new GameDTO(playerNamesDTO, questionDTO, 0);
    }

    private List<Question> validatInputAndReturnFilteredQuestions(GameRequestDTO gameRequest) throws ValidationException {
        List<Question> questions = questionDao.findAll();

        String category = gameRequest.getCategory();
        List<String> uniqueCategories = questions.stream()
                .map(Question::getCategory)
                .distinct()
                .toList();

        if (category == null || !uniqueCategories.contains(category)) {
            throw new ValidationException("Invalid category: " + category);
        }

        String difficulty = gameRequest.getDifficulty();
        if (difficulty != null &&
                (!difficulty.equals("EASY") &&
                        !difficulty.equals("MEDIUM") &&
                        !difficulty.equals("HARD"))) {
            throw new ValidationException("Invalid difficulty: " + difficulty);
        }

        int limit = gameRequest.getLimit();
        if (limit < 0 || limit > 50) {
            throw new ValidationException("Invalid question limit: " + limit);
        }

        return questions.stream()
                .filter(question -> question.getDifficulty().name().equals(difficulty))
                .limit(limit)
                .toList();
    }

    public Integer getNumberOfPlayers(Context ctx) {
        String numberOfPlayers = ctx.pathParam("number");
        return gameService.createNumberOfPlayers(Integer.parseInt(numberOfPlayers), getUsernameFromJwt(ctx));
    }

    public PlayerNamesDTO createPlayers(Context ctx) {
        PlayerNamesDTO playerNamesDTO = ctx.bodyAsClass(PlayerNamesDTO.class);
        String gameidStr = ctx.pathParam("gameid");
        Integer gameid = Integer.parseInt(gameidStr);
        gameService.createPlayers(playerNamesDTO.players(), gameid);
        return playerNamesDTO;
    }

    public QuestionBody getOneQuestion() {
        Question q = questionDao.findById(1);
        return new QuestionBody(q.getDifficulty().toString(), q.getCategory(), q.getDescription(), q.getRightAnswer(), q.getWrongAnswers());
    }

    public PlayerNamesDTO getScore(Context ctx) {
        Integer gameId = Integer.parseInt(ctx.pathParam("gameid"));
        PlayerNamesDTO scoresAndNames = gameService.getScores(gameId);
        return scoresAndNames;
    }

    public GameDTO saveGame(Context ctx) {
        Integer gameId = Integer.parseInt(ctx.pathParam("gameid"));
        Integer turn = Integer.parseInt(ctx.pathParam("turn"));

        GameDTO updatedGame = ctx.bodyAsClass(GameDTO.class);

        GameDTO gameDTO = gameService.updateGame(gameId, turn, updatedGame);

        return gameDTO;
    }

    public GameDTO getSavedGame(Context ctx) {
        Integer gameId = Integer.parseInt(ctx.pathParam("gameid"));

        GameDTO savedGameDTO = gameService.getGame(gameId);

        return savedGameDTO;
    }

    public static String getUsernameFromJwt(Context ctx) {
        String token = ctx.header("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Invalid or missing Authorization header");
        }
        String SECRET_KEY;

        if (System.getenv("DEPLOYED") != null) {
            SECRET_KEY = System.getenv("SECRET_KEY");
        } else {
            SECRET_KEY = Utils.getPropertyValue("SECRET_KEY", "config.properties");
        }

        token = token.substring(7); // Remove "Bearer " prefix
        Claims claims = Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject(); // Assuming the username is stored in the "sub" claim
    }
}