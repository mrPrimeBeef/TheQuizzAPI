package app;

import app.config.ApplicationConfig;
import app.config.HibernateConfig;
import app.controller.GameController;
import app.controller.SecurityController;
import app.rest.Routes;
import jakarta.persistence.EntityManagerFactory;

public class Main {
    public static void main(String[] args) {
        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();

//        Populator.questionAndUserData(emf);

        SecurityController securityController = new SecurityController();
        GameController gameController = new GameController();

        Routes routes = new Routes(securityController, gameController);

        ApplicationConfig
                .getInstance()
                .initiateServer()
                .checkSecurityRoles()
                .setRoute(routes.getRoutes())
                .handleException()
                .startServer(7070);
    }
}