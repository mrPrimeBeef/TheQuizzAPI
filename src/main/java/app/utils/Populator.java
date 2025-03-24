package app.utils;


import app.daos.QuestionDao;
import app.daos.SecurityDAO;
import app.entities.Question;
import app.entities.User;
import app.entities.enums.Role;
import app.services.OpentdbService;
import jakarta.persistence.EntityManagerFactory;

import java.util.List;

public class Populator {
    public static void questionAndUserData(EntityManagerFactory emf) {
        addQuestions(emf);
        usersAndRoles(emf);
    }

    private static void usersAndRoles(EntityManagerFactory emf) {
        SecurityDAO securityDAO = SecurityDAO.getInstance(emf);

        User user1 = new User("Villager", "1234");
        User user2 = new User("PineBoxJim", "4321");

        user1.addRole(Role.USER);
        user2.addRole(Role.ADMIN);

        securityDAO.createUser(user1.getUsername(),user1.getPassword());
        securityDAO.createUser(user2.getUsername(),user2.getPassword());
    }

    private static void addQuestions(EntityManagerFactory emf) {
        QuestionDao questionDao = QuestionDao.getInstance();
        OpentdbService opentdbService = new OpentdbService();

        List<Question> questions = opentdbService.getComputerSienceQuestions();

        for (Question q : questions){
            questionDao.create(q);
        }
    }
}
