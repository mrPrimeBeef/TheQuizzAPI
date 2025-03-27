package app.rest;

import app.controller.GameController;
import app.controller.ISecurityController;
import app.controller.SecurityController;
import app.dtos.GameDTO;
import app.dtos.QuestionBody;
import app.entities.enums.Role;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.Context;

import java.util.Map;

import static io.javalin.apibuilder.ApiBuilder.*;

public class Routes {
    private final ISecurityController securityController;
    private final GameController gameController;

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
            post("/players/{number}", (ctx) -> {
                try {
                    gameController.getNumberOfPlayers(ctx);
                } catch (Exception e) {
                    handlePostException(ctx, e);
                }
            }, Role.ADMIN, Role.USER);

            post("/players", (ctx) -> {
                try {
                    gameController.createPlayers(ctx);
                } catch (Exception e) {
                    handlePostException(ctx, e);
                }
            }, Role.ADMIN, Role.USER);

            get("/questions?limit={number}&category={category}&difficulty={difficulty}", (ctx) -> {
                try {
                    GameDTO gameDTO = gameController.makeGame(ctx);
                    ctx.status(200).json(gameDTO);
                } catch (Exception e) {
                    handleGetException(ctx, e);
                }
            }, Role.ADMIN, Role.USER);

            post("/populate", (ctx) -> {
                try {
                    gameController.populateDatabaseRoles(ctx);
                    gameController.populateDatabaseWithScienceComputersQuestions(ctx);
                    ctx.status(200);
                } catch (Exception e) {
                    handlePostException(ctx, e);
                }
            }, Role.ANYONE);
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
                } catch (Exception e) {
                    handlePostException(ctx, e);
                }
            }, Role.ANYONE);
            post("/register", (ctx) -> {
                try {
                    securityController.register(ctx);
                } catch (Exception e) {
                    handlePostException(ctx, e);
                }
            }, Role.ANYONE);
        };
    }

    private void handlePostException(Context ctx, Exception e) {
        ctx.status(400).json(Map.of("status", 400, "msg", "Ugyldig anmodning (f.eks. manglende felt)"));
    }

    private void handleGetException(Context ctx, Exception e) {
        ctx.status(404).json(Map.of("status", 404, "msg", "Ressource ikke fundet (spil, spørgsmål osv.)"));
    }
}