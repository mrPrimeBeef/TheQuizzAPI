package app.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.javalin.Javalin;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.config.JavalinConfig;
import io.javalin.http.Context;

import app.controller.ISecurityController;
import app.controller.SecurityController;

public class ApplicationConfig {
    private static ApplicationConfig applicationConfig;
    private static Javalin app;
    private static JavalinConfig javalinConfig;
    private static ObjectMapper objectMapper = new ObjectMapper();
    private static ISecurityController securityController = new SecurityController();

    private ApplicationConfig() {
    }

    public static ApplicationConfig getInstance() {
        if (applicationConfig == null) {
            applicationConfig = new ApplicationConfig(); // Gemmer instansen!
        }
        return applicationConfig;
    }

    public ApplicationConfig initiateServer() {
        app = Javalin.create(config -> {
            javalinConfig = config;
            config.http.defaultContentType = "application/json";
            config.router.contextPath = "/api";
            config.bundledPlugins.enableRouteOverview("/routes");
            config.bundledPlugins.enableDevLogging();
            config.showJavalinBanner = true;
        });

//        app.before(ApplicationConfig::corsHeaders);
//        app.options("/*", ApplicationConfig::corsHeadersOptions);

        return applicationConfig;
    }

    public ApplicationConfig setRoute(EndpointGroup route) {
        javalinConfig.router.apiBuilder(route);
        return applicationConfig;
    }

    public ApplicationConfig handleException() {
        app.exception(IllegalStateException.class, (e, ctx) -> {
            handleError(ctx, 400, "Invalid input: " + e.getMessage());
        });

        app.exception(Exception.class, (e, ctx) -> {
            handleError(ctx, 500, "Something went wrong: " + e.getMessage());
        });

        return this;
    }

    private void handleError(Context ctx, int statusCode, String message) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("msg", message);
        ctx.status(statusCode);
        ctx.json(node);
    }

    public ApplicationConfig checkSecurityRoles() {
        app.beforeMatched(securityController::accessHandler); // authenticate and authorize
        return applicationConfig;
    }

    public ApplicationConfig startServer(int port) {
        app.start(port);
        return applicationConfig;
    }

    //TODO Kig på en bedre måde at gøre dette
    private static void corsHeaders(Context ctx) {
        ctx.header("Access-Control-Allow-Origin", "*");
        ctx.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        ctx.header("Access-Control-Allow-Headers", "Content-Type, Authorization");
        ctx.header("Access-Control-Allow-Credentials", "true");
    }

    private static void corsHeadersOptions(Context ctx) {
        ctx.header("Access-Control-Allow-Origin", "*");
        ctx.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        ctx.header("Access-Control-Allow-Headers", "Content-Type, Authorization");
        ctx.header("Access-Control-Allow-Credentials", "true");
        ctx.status(204);
    }

    public static void stopServer() {
        app.stop();
        app = null;
    }
}