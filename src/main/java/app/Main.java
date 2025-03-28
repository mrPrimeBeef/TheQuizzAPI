package app;

import app.config.ApplicationConfig;
import app.config.HibernateConfig;
import app.controller.GameController;
import app.controller.SecurityController;
import app.daos.GameDao;
import app.daos.PlayerDao;
import app.daos.RoleDao;
import app.rest.Routes;
import app.services.GameService;
import jakarta.persistence.EntityManagerFactory;

public class Main {
    public static void main(String[] args) {
        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();

        PlayerDao playerDao = PlayerDao.getInstance(emf);
        GameDao gameDao = GameDao.getInstance(emf);
        RoleDao roleDao = RoleDao.getInstance(emf);
        GameService gameService = new GameService(gameDao, playerDao);

        SecurityController securityController = new SecurityController(emf, roleDao);
        GameController gameController = new GameController(gameService, emf);

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