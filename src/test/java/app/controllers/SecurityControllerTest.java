package app.controllers;

import app.config.ApplicationConfig;
import app.config.HibernateConfig;
import app.controller.AdminController;
import app.controller.GameController;
import app.controller.SecurityController;
import app.daos.*;
import app.entities.Role;
import app.entities.User;
import app.rest.Routes;
import app.services.GameService;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SecurityControllerTest {

    private static final EntityManagerFactory emf = HibernateConfig.getEntityManagerFactoryForTest();
    private final Logger logger = LoggerFactory.getLogger(SecurityControllerTest.class.getName());
    private final String TEST_USER = "testuser";
    private final String TEST_PASSWORD = "password123";
    private final String TEST_ADMIN = "testadmin";
    private final Role userRole = new Role("USER");
    private final Role adminRole = new Role("ADMIN");

    @BeforeAll
    static void setUpAll() {
        RoleDao roleDao = RoleDao.getInstance(emf);
        GameDao gameDao = GameDao.getInstance(emf);
        PlayerDao playerDao = PlayerDao.getInstance(emf);

        GameService gameService = new GameService(gameDao, playerDao);

        SecurityController securityController = new SecurityController(emf, roleDao);
        GameController gameController = new GameController(gameService, emf);
        AdminController adminController = new AdminController(emf);
        Routes routes = new Routes(securityController, gameController, adminController);

        ApplicationConfig
                .getInstance()
                .initiateServer()
                .setRoute(routes.getRoutes())
                .handleException()
                .checkSecurityRoles()
                .startServer(7079);
        RestAssured.baseURI = "http://localhost:7079/api";
    }

    @BeforeEach
    void setUp() {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            // Clean up existing data
            em.createNativeQuery("DELETE FROM user_role").executeUpdate();
            em.createQuery("DELETE FROM User").executeUpdate();
            em.createQuery("DELETE FROM Role").executeUpdate();


            // Persist roles so users can be persistet
            em.persist(userRole);
            em.persist(adminRole);

            // Create test user with user role
            User testUserAccount = new User(TEST_USER, TEST_PASSWORD);
            testUserAccount.addRole(userRole);
            em.persist(testUserAccount);

            // Create test admin with admin role
            User testAdmin = new User(TEST_ADMIN, TEST_PASSWORD);
            testAdmin.addRole(userRole);
            testAdmin.addRole(adminRole);
            em.persist(testAdmin);

            em.getTransaction().commit();
        }
    }

    @Test
    void healtcheck_test() {
        given()
                .when()
                .get("/auth/healthcheck")
                .then()
                .statusCode(200)
                .body("msg", equalTo("API is up and running"));
    }

    @Test
    void testLogin_Success() {
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", TEST_USER);
        loginRequest.put("password", TEST_PASSWORD);

        given()
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .when()
                .post("/auth/login")
                .then()
                .statusCode(200)
                .body("token", notNullValue())
                .body("username", equalTo(TEST_USER));
    }

    @Test
    void testLogin_WrongPassword() {
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", TEST_USER);
        loginRequest.put("password", "wrongpassword");

        given()
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .when()
                .post("/auth/login")
                .then()
                .statusCode(400);
    }

    @Test
    void testLogin_UserNotFound() {
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", "nonexistentuser");
        loginRequest.put("password", TEST_PASSWORD);

        given()
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .when()
                .post("/auth/login")
                .then()
                .statusCode(400);
    }

    @Test
    void testRegister_Success() {
        Map<String, String> registerRequest = new HashMap<>();
        registerRequest.put("username", "newTestUser");
        registerRequest.put("password", "newpassword");

        Response response = given()
                .contentType("application/json")
                .body(registerRequest)
                .when()
                .post("/auth/register")
                .then()
                .extract().response();

        response.then()
                .statusCode(201)
                .body("token", notNullValue())
                .body("username", equalTo("newTestUser"));
    }

    @Test
    void testRegister_UserAlreadyExists() {
        // Count users before the test
        int userCountBefore = countUsers();

        // Try to register an existing user
        Map<String, String> registerRequest = new HashMap<>();
        registerRequest.put("username", TEST_USER);
        registerRequest.put("password", TEST_PASSWORD);

        try {
            given()
                    .contentType(ContentType.JSON)
                    .body(registerRequest)
                    .when()
                    .post("/auth/register");
        } catch (Exception e) {
            // Ignore any exceptions - we expect this to fail
            logger.info("Expected exception: {}", e.getMessage());
        }

        // Count users after the test
        int userCountAfter = countUsers();

        // Verify that no new user was created
        assertEquals(userCountBefore, userCountAfter, "User count should not change when trying to register an existing user");
    }

    private int countUsers() {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery("SELECT COUNT(u) FROM User u", Long.class).getSingleResult().intValue();
        }
    }

    @Test
    void testProtectedUserEndpoint_WithUserRole() {
        // First login to get a token
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", TEST_ADMIN);
        loginRequest.put("password", TEST_PASSWORD);

        Response loginResponse = given()
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .post("/auth/login");

        String token = loginResponse.jsonPath().getString("token");

        // Then access protected user endpoint
        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/admin/test")
                .then()
                .body("msg", containsString("Hello from Admin"))
                .statusCode(200);
    }

    @Test
    void testProtectedAdminEndpoint_WithUserRole() {
        // First login to get a token for a user with only USER role
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", TEST_USER);
        loginRequest.put("password", TEST_PASSWORD);

        Response loginResponse = given()
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .post("/auth/login");

        String token = loginResponse.jsonPath().getString("token");

        // Then try to access protected admin endpoint
        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/admin/populate")
                .then()
                .statusCode(403); // Forbidden
    }
}