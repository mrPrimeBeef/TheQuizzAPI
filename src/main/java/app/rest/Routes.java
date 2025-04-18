package app.rest;

import java.util.Map;

import static io.javalin.apibuilder.ApiBuilder.*;

import io.javalin.http.Context;
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
                try {
                    Integer gameId = gameController.getNumberOfPlayers(ctx);
                    ctx.status(201);
                } catch (Exception e) {
                    handlePostException(ctx, e);
                }
            }, Role.ADMIN, Role.USER);

            post("/{gameid}/players/names", (ctx) -> {
                try {
                    PlayerNamesDTO players = gameController.createPlayers(ctx);
                    ctx.status(201).json(players);
                } catch (Exception e) {
                    handlePostException(ctx, e);
                }
            }, Role.ADMIN, Role.USER);

            get("/{gameid}/questions", (ctx) -> {
                try {
                    GameDTO gameDTO = gameController.makeGame(ctx);
                    ctx.status(201).json(gameDTO);
                } catch (Exception e) {
                    handleGetException(ctx, e);
                }
            }, Role.ADMIN, Role.USER);

            get("/{gameid}/score", (ctx) -> {
                try {
                    PlayerNamesDTO playerNamesDTO = gameController.getScore(ctx);
                    ctx.status(200).json(playerNamesDTO);
                } catch (Exception e) {
                    handleGetException(ctx, e);
                }
            }, Role.USER, Role.ADMIN);

            //TODO check functionality
            post("/{playerid}/{questionid}/answer", (ctx) -> {
                try {
                    gameController.updateScore(ctx);
                    PlayerNamesDTO playerNamesDTO = gameController.getScore(ctx);
                    ctx.status(200).json(playerNamesDTO);
                } catch (Exception e) {
                    handlePostException(ctx, e);
                }
            }, Role.ADMIN, Role.USER);
        };
    }

    private EndpointGroup authRoutes() {
        return () -> {
            get("/test", ctx -> {
                try {
                    QuestionBody questionBody = gameController.getOneQuestion();
                    ctx.status(200).json(questionBody);
                } catch (Exception e) {
                    handleGetException(ctx, e);
                }
            }, Role.ANYONE);

            get("/healthcheck", securityController::healthCheck, Role.ANYONE);
            post("/login", (ctx) -> {
                try {
                    securityController.login(ctx);
                    ctx.status(200);
                } catch (Exception e) {
                    handlePostException(ctx, e);
                }
            }, Role.ANYONE);
            post("/register", (ctx) -> {
                try {
                    securityController.register(ctx);
                    ctx.status(200);
                } catch (Exception e) {
                    handlePostException(ctx, e);
                }
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
                    handlePostException(ctx, e);
                }
            }, Role.ADMIN);

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

    private void handlePostException(Context ctx, Exception e) {
        ctx.status(400).json(Map.of("status", 400, "msg", "Ugyldig anmodning (f.eks. manglende felt)"));
    }

    private void handleGetException(Context ctx, Exception e) {
        ctx.status(404).json(Map.of("status", 404, "msg", "Ressource ikke fundet (spil, spørgsmål osv.)"));
    }
}