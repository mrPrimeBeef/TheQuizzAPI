package app.rest;

import static io.javalin.apibuilder.ApiBuilder.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.apibuilder.EndpointGroup;

import app.controller.GameController;
import app.controller.ISecurityController;
import app.controller.SecurityController;
import app.dtos.GameDTO;
import app.dtos.PlayerNamesDTO;
import app.dtos.QuestionBody;
import app.entities.enums.Role;
import app.controller.AdminController;

public class Routes {
    private final ISecurityController securityController;
    private final GameController gameController;
    private final AdminController adminController;
    private final ObjectMapper jsonMapper = new ObjectMapper();

    public Routes(SecurityController securityController, GameController gameController, AdminController adminController) {
        this.gameController = gameController;
        this.adminController = adminController;
        this.securityController = securityController;
    }

    public EndpointGroup getRoutes() {
        return () -> {
            path("game", protectedGameRoutes());
            path("auth", authRoutes());
            path("admin", adminRoutes());
        };
    }

    private EndpointGroup protectedGameRoutes() {
        return () -> {
            post("/{number}", (ctx) -> {

                Integer gameId = gameController.getNumberOfPlayers(ctx);
                ctx.status(201).json(gameId);

            }, Role.USER, Role.ADMIN);

            post("/{gameid}/players/names", (ctx) -> {
                PlayerNamesDTO players = gameController.createPlayers(ctx);
                ctx.status(201).json(players);
            }, Role.USER, Role.ADMIN);

            post("/{gameid}/questions", (ctx) -> {
                GameDTO gameDTO = gameController.makeGame(ctx);
                ctx.status(201).json(gameDTO);
            }, Role.USER, Role.ADMIN);

            get("/{gameid}/score", (ctx) -> {
                PlayerNamesDTO playerNamesDTO = gameController.getScore(ctx);
                ctx.status(200).json(playerNamesDTO);
            }, Role.USER, Role.ADMIN);

            post("/savegame/{gameid}/{turn}", (ctx) -> {
                GameDTO savedGameDTO = gameController.saveGame(ctx);
                ctx.status(201).json(savedGameDTO);
            }, Role.ADMIN, Role.USER);

            get("/savegame/{gameid}", (ctx) -> {
                GameDTO savedGameDTO = gameController.getSavedGame(ctx);
                ctx.status(201).json(savedGameDTO);
            }, Role.ADMIN, Role.USER);
        };
    }

    private EndpointGroup authRoutes() {
        return () -> {
            get("/test", ctx -> {
                QuestionBody questionBody = gameController.getOneQuestion();
                ctx.status(200).json(questionBody);

            }, Role.ANYONE);

            get("/healthcheck", securityController::healthCheck, Role.ANYONE);
            post("/login", (ctx) -> {
                securityController.login(ctx);
                ctx.status(200);

            }, Role.ANYONE);
            post("/register", (ctx) -> {
                securityController.register(ctx);
                ctx.status(201);
            }, Role.ANYONE);
        };
    }

    private EndpointGroup adminRoutes() {
        return () -> {
            get("/test", ctx -> ctx.json(jsonMapper.createObjectNode().put("msg", "Hello from Admin"))
                    , Role.ADMIN);

            get("populate", (ctx) -> {
                try {
                    try {
                        adminController.populateDatabaseRoles();
                    } catch (Exception e) {
                    } finally {
                        adminController.populateDatabaseWithScienceComputersQuestions();
                        ctx.status(200).result("Database now got data in it");
                    }
                    ctx.status(200);
                } catch (Exception e) {
                }
            }, Role.ADMIN, Role.ANYONE);

            put("/question", ctx -> {
                int id = adminController.createQuestion(ctx);
                ctx.status(200).result("Question with id" + id + "created successfully");
            });

            //TODO check functionality
            delete("/question/{questionid}", ctx -> {
                adminController.deleteQuestion(ctx);
                ctx.status(200).result("The question is in the database is now gone");
            });

        };
    }
}