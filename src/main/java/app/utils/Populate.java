package app.utils;

import app.daos.QuestionDao;
import app.daos.SecurityDAO;
import app.entities.Question;
import app.entities.User;
import app.entities.Role;
import app.services.OpentdbService;
import jakarta.persistence.EntityManagerFactory;

import java.util.List;

public class Populate {
    public static void questionAndUserData(EntityManagerFactory emf) {
        SecurityDAO securityDAO = SecurityDAO.getInstance(emf);

        securityDAO.createRolesInDataBase();
        addQuestions(emf);
        usersAndRoles(emf);
    }

    public static void usersAndRoles(EntityManagerFactory emf) {
        SecurityDAO securityDAO = SecurityDAO.getInstance(emf);

        User user1 = new User("Villager", "1234");
        User user2 = new User("PineBoxJim", "4321");

        Role userRole = new Role("USER");
        Role adminRole = new Role("ADMIN");

        user1.addRole(userRole);
        user2.addRole(adminRole);

        securityDAO.createUser(user1);
        securityDAO.createUser(user2);
    }

    public static void addQuestions(EntityManagerFactory emf) {
        QuestionDao questionDao = QuestionDao.getInstance(emf);
        OpentdbService opentdbService = new OpentdbService();

        List<Question> questions = opentdbService.getComputerSienceQuestions();

        for (Question q : questions) {
            questionDao.create(q);
        }
    }
}
