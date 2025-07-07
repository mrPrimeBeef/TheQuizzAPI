package app.utils;

import app.daos.QuestionDao;
import app.daos.RoleDao;
import app.daos.SecurityDAO;
import app.entities.Question;
import app.entities.User;
import app.entities.Role;
import app.services.OpentdbService;
import jakarta.persistence.EntityManagerFactory;

import java.util.List;

public class Populate {
    public static void questionAndUserData(EntityManagerFactory emf, RoleDao roleDao) {
        SecurityDAO securityDAO = SecurityDAO.getInstance(emf, roleDao);

        securityDAO.createRolesInDataBaseAndAdmin();
        addComputerSienceQuestions(emf);
        usersAndRoles(securityDAO);
    }

    public static void usersAndRoles(SecurityDAO securityDAO) {
        RoleDao roleDao = securityDAO.getRoleDao();

        Role userRole;
        try {
            userRole = roleDao.findById("USER");
        } catch (Exception e) {
            userRole = new Role("USER");
            roleDao.create(userRole);
        }

        Role adminRole;
        try {
            adminRole = roleDao.findById("ADMIN");
        } catch (Exception e) {
            adminRole = new Role("ADMIN");
            roleDao.create(adminRole);
        }

        User user1 = new User("Villager", "1234");
        User user2 = new User("PineBoxJim", "4321");

        user1.addRole(userRole);
        user2.addRole(adminRole);

        securityDAO.createUser(user1);
        securityDAO.createUser(user2);
    }

    public static void addComputerSienceQuestions(EntityManagerFactory emf) {
        QuestionDao questionDao = QuestionDao.getInstance(emf);
        OpentdbService opentdbService = new OpentdbService();

        String url = "https://opentdb.com/api.php?amount=50&category=18&token=";
        List<Question> questions = opentdbService.getQuestionsFromURL(url, 3);

        for (Question q : questions) {
            questionDao.create(q);
        }
    }

    public static void addGeneralKnowledgeQuestions(EntityManagerFactory emf) {
        QuestionDao questionDao = QuestionDao.getInstance(emf);
        OpentdbService opentdbService = new OpentdbService();

        String url = "https://opentdb.com/api.php?amount=50&category=9";
        List<Question> questions = opentdbService.getQuestionsFromURL(url, 5);

        for (Question q : questions) {
            questionDao.create(q);
        }
    }

    public static void addSienceAndNatureQuestions(EntityManagerFactory emf) {
        QuestionDao questionDao = QuestionDao.getInstance(emf);
        OpentdbService opentdbService = new OpentdbService();

        String url = "https://opentdb.com/api.php?amount=50&category=17";
        List<Question> questions = opentdbService.getQuestionsFromURL(url, 5);

        for (Question q : questions) {
            questionDao.create(q);
        }
    }

    public static void addEntertainmentFilmQuestions(EntityManagerFactory emf) {
        QuestionDao questionDao = QuestionDao.getInstance(emf);
        OpentdbService opentdbService = new OpentdbService();

        String url = "https://opentdb.com/api.php?amount=50&category=11";
        List<Question> questions = opentdbService.getQuestionsFromURL(url, 6);

        for (Question q : questions){
            questionDao.create(q);
        }
    }
}