package app.utils;


import app.daos.QuestionDao;
import app.daos.RoleDao;
import app.daos.UserDao;
import app.entities.Question;
import app.entities.Role;
import app.entities.User;
import app.services.OpentdbService;
import jakarta.persistence.EntityManagerFactory;

import java.util.List;

public class Populator {
    public static void questionData(EntityManagerFactory emf) {
        addQuestions(emf);
        usersAndRoles(emf);
    }

    private static void usersAndRoles(EntityManagerFactory emf) {
        UserDao userDao = UserDao.getInstance();
        RoleDao roleDao = RoleDao.getInstance();

        User user1 = new User("Villager", "1234");
        User user2 = new User("PineBoxJim", "4321");

        Role user = new Role("user");
        Role admin = new Role("admin");

        roleDao.create(user);
        roleDao.create(admin);

        user1.addRole(user);
        user2.addRole(admin);

        userDao.create(user1);
        userDao.create(user2);
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
