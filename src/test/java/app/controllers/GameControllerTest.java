package app.controllers;

import app.config.ApplicationConfig;
import app.config.HibernateConfig;
import app.controller.GameController;
import app.controller.SecurityController;
import app.daos.GameDao;
import app.daos.PlayerDao;
import app.daos.QuestionDao;
import app.daos.RoleDao;
import app.entities.*;
import app.entities.enums.Difficulty;
import app.rest.Routes;
import app.services.GameService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dk.bugelhartmann.UserDTO;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GameControllerTest {
    private static final EntityManagerFactory emf = HibernateConfig.getEntityManagerFactoryForTest();
    private static final String TEST_PASSWORD = "password123";
    private static final String TEST_ADMIN = "testadmin";
    private static final Role userRole = new Role("USER");
    private static final Role adminRole = new Role("ADMIN");
    private ObjectMapper objectMapper = new ObjectMapper();
    private String adminToken;

    @BeforeAll
    static void setUpAll() {
        RoleDao roleDao = RoleDao.getInstance(emf);
        GameDao gameDao = GameDao.getInstance(emf);
        PlayerDao playerDao = PlayerDao.getInstance(emf);

        GameService gameService = new GameService(gameDao, playerDao);

        SecurityController securityController = new SecurityController(emf, roleDao);
        GameController gameController = new GameController(gameService, emf);
        Routes routes = new Routes(securityController, gameController);

        ApplicationConfig
                .getInstance()
                .initiateServer()
                .setRoute(routes.getRoutes())
                .handleException()
                .checkSecurityRoles()
                .startServer(7079);
        RestAssured.baseURI = "http://localhost:7079/api";

        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.persist(userRole);
            em.persist(adminRole);
            User testAdmin = new User(TEST_ADMIN, TEST_PASSWORD);
            testAdmin.addRole(userRole);
            testAdmin.addRole(adminRole);
            em.persist(testAdmin);
            em.getTransaction().commit();
        }
    }

    @BeforeEach
    void setUp() {
        adminToken = getAdminToken();

        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            // Clean up existing data
            em.createNativeQuery("DELETE FROM game_question").executeUpdate();
            em.createNativeQuery("DELETE FROM question_wronganswers").executeUpdate();
            em.createQuery("DELETE FROM Question").executeUpdate();
            em.createQuery("DELETE FROM Player").executeUpdate();
            em.createQuery("DELETE FROM Game").executeUpdate();

            // Create test game with players
            List<Player> players = new ArrayList<>();
            players.add(new Player(0, "testPlayer1"));
            players.add(new Player(0, "testPlayer2"));


            List<String> wrong = new ArrayList<>();
            wrong.add("false1");
            wrong.add("false2");
            List<Question> questions = new ArrayList<>();
            Question question1 = new Question("test1", "true", wrong, "test", Difficulty.EASY);
            Question question2 = new Question("test2", "true", wrong, "test", Difficulty.EASY);
            questions.add(question1);
            questions.add(question2);

            em.persist(question1);
            em.persist(question2);

            Game testGame = new Game(players, questions, 2);
            em.persist(testGame);

            em.getTransaction().commit();
        }
    }

    @Test
    void testNumberOfPlayersToGame() {
        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .post("/game/2")
                .then()
                .statusCode(201);
    }

    private String getAdminToken() {
        UserDTO adminUser = new UserDTO(TEST_ADMIN, TEST_PASSWORD);

        String adminJson;
        try {
            adminJson = objectMapper.writeValueAsString(adminUser);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Kunne ikke konvertere admin-bruger til JSON", e);
        }

        // Send login-anmodning og hent token
        String token = given()
                .contentType("application/json")
                .body(adminJson)
                .when()
                .post("/auth/login")
                .then()
                .statusCode(200)
                .extract().path("token");

        return token;
    }
}