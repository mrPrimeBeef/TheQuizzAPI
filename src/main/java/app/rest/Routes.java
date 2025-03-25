package app.rest;

import app.controller.GameController;
import app.controller.ISecurityController;
import app.controller.SecurityController;
import app.dtos.GameDTO;
import app.dtos.QuestionBody;
import app.entities.Player;
import app.entities.enums.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.apibuilder.EndpointGroup;

import java.util.List;
import java.util.Map;

import static io.javalin.apibuilder.ApiBuilder.*;

public class Routes {
    private final ISecurityController securityController;
    private final GameController gameController;
    private final ObjectMapper jsonMapper = new ObjectMapper();

    public Routes(SecurityController securityController, GameController gameController) {
        this.gameController = gameController;
        this.securityController = securityController;
    }

    public EndpointGroup getRoutes() {
        return () -> {
            path("game", protectedGameRoutes());
            path("auth", authRoutes());
        };
    }

    private EndpointGroup protectedGameRoutes() {
        return () -> {
            post("/api/game/players/{number}", (ctx) -> {
                try {
                    //TODO find ud af gemme dette tal, så det bruges i create players
                    gameController.getNumberOfPlayers(ctx);
                } catch (Exception e) {
                    ctx.status(404).json(Map.of("msg", e.getMessage()));
                }
            }, Role.ADMIN, Role.USER);

            post("/api/game/players", (ctx) -> {
                try {
                    gameController.createPlayers(ctx);
                } catch (Exception e) {
                    ctx.status(404).json(Map.of("msg", e.getMessage()));
                }
            }, Role.ADMIN, Role.USER);

            get("/questions?limit={number}&category={category}&difficulty={difficulty}", (ctx) -> {
                try {
                    GameDTO gameDTO = gameController.makeGame(ctx);
                    ctx.status(200).json(gameDTO);
                } catch (Exception e) {
                    ctx.status(404).json(Map.of("msg", e.getMessage()));
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
                    ctx.status(404).json(Map.of("msg", e.getMessage()));
                }
            }, Role.ANYONE, Role.USER, Role.ADMIN);

            get("/healthcheck", securityController::healthCheck, Role.ANYONE);
            post("/login", securityController::login, Role.ANYONE);
            post("/register", securityController::register, Role.ANYONE);
        };
    }
}