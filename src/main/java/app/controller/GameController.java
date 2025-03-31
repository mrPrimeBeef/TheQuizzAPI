package app.controller;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import io.javalin.http.Context;
import jakarta.persistence.EntityManagerFactory;

import app.daos.*;
import app.dtos.*;
import app.entities.Game;
import app.entities.Player;
import app.entities.Question;
import app.exceptions.ValidationException;
import app.services.GameService;
import app.utils.Populate;
import org.jetbrains.annotations.NotNull;


public class GameController {
    private final GameService gameService;
    private QuestionDao questionDao;
    private PlayerDao playerDao;
    private RoleDao roleDao;
    private GameDao gameDao;
    private SecurityDAO securityDAO;
    private EntityManagerFactory emf;

    public GameController(GameService gameService, EntityManagerFactory emf) {
        this.gameService = gameService;
        this.questionDao = QuestionDao.getInstance(emf);
        this.playerDao = PlayerDao.getInstance(emf);
        this.roleDao = RoleDao.getInstance(emf);
        this.gameDao = GameDao.getInstance(emf);
        this.securityDAO = SecurityDAO.getInstance(emf, roleDao);

    }

    public GameDTO makeGame(Context ctx) {
        try {
            List<Question> filteredQuestions = validatInputAndReturnFilteredQuestions(ctx);

            Integer gameid = Integer.parseInt(ctx.pathParam("gameid"));
            List<Player> players = playerDao.findAllPlayersByGameId(gameid);
            Game activeGame = gameDao.findById(gameid);

            gameService.createGame(players, filteredQuestions, activeGame);

            // this is to send DTO of the game
            return getGameDTO(players, filteredQuestions);
        } catch (Exception e) {

        }
        return null;
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

        return new GameDTO(playerNamesDTO, questionDTO);
    }

    private @NotNull List<Question> validatInputAndReturnFilteredQuestions(Context ctx) throws ValidationException {
        String limitStr = ctx.queryParam("limit");

        List<Question> questions = questionDao.findAll();

        String category = URLDecoder.decode(ctx.queryParam("category"), StandardCharsets.UTF_8);

        List<String> uniqueCategories = questions.stream()
                .map(Question::getCategory)
                .distinct()
                .collect(Collectors.toList());

        if (category == null || !uniqueCategories.contains(category)) {
            throw new ValidationException("From makeGame(), cannot make a game with the category " + category);
        }

        //TODO hardcoded values change if more than 3 difficulites
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
                .filter(question -> question.getDifficulty().name().equals(difficulty))
                .limit(limit)
                .collect(Collectors.toList());
        return filteredQuestions;
    }

    public Integer getNumberOfPlayers(Context ctx) {
        String numberOfPlayers = ctx.pathParam("number");
        return gameService.createNumberOfPlayers(Integer.parseInt(numberOfPlayers));
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

    public void populateDatabaseWithScienceComputersQuestions() {
        Populate.addQuestions(emf);
    }

    public void populateDatabaseRoles() {
        Populate.usersAndRoles(securityDAO);
    }

    public PlayerNamesDTO getScore(Context ctx) {
        Integer gameId = Integer.parseInt(ctx.pathParam("gameid"));
        PlayerNamesDTO scoresAndNames = gameService.getScores(gameId);
        return scoresAndNames;
    }
}
