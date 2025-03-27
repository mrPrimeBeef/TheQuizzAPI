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
        addQuestions(emf);
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

    public static void addQuestions(EntityManagerFactory emf) {
        QuestionDao questionDao = QuestionDao.getInstance(emf);
        OpentdbService opentdbService = new OpentdbService();

        List<Question> questions = opentdbService.getComputerSienceQuestions();

        for (Question q : questions) {
            questionDao.create(q);
        }
    }
}