package app;

import app.controller.AdminController;
import jakarta.persistence.EntityManagerFactory;

import app.config.ApplicationConfig;
import app.config.HibernateConfig;
import app.controller.GameController;
import app.controller.SecurityController;
import app.daos.GameDao;
import app.daos.PlayerDao;
import app.daos.RoleDao;
import app.rest.Routes;
import app.services.GameService;

public class Main {
    public static void main(String[] args) {
        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();

        PlayerDao playerDao = PlayerDao.getInstance(emf);
        GameDao gameDao = GameDao.getInstance(emf);
        RoleDao roleDao = RoleDao.getInstance(emf);
        GameService gameService = new GameService(gameDao, playerDao);

        SecurityController securityController = new SecurityController(emf, roleDao);
        GameController gameController = new GameController(gameService, emf);
        AdminController adminController = new AdminController(emf);

        Routes routes = new Routes(securityController, gameController, adminController);

        ApplicationConfig
                .getInstance()
                .initiateServer()
                .checkSecurityRoles()
                .setRoute(routes.getRoutes())
                .handleException()
                .startServer(7070);
    }
}