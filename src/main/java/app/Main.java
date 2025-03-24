package app;

import app.config.ApplicationConfig;
import app.config.HibernateConfig;
import app.rest.Routes;
import app.utils.Populator;
import jakarta.persistence.EntityManagerFactory;

public class Main {
    public static void main(String[] args) {
        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
        // Populator.questionData(emf);

        ApplicationConfig
                .getInstance()
                .initiateServer()
                .checkSecurityRoles()
                .setRoute(Routes.getRoutes())
//                .setRoute(Routes.getSecureRoutes())
                .handleException()
                .startServer(7070);
    }
}