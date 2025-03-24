package app.utils;


import app.daos.RoleDao;
import app.daos.UserDao;
import app.entities.Role;
import app.entities.User;
import jakarta.persistence.EntityManagerFactory;

public class Populator {
    public static void testData(EntityManagerFactory emf) {
        //addQuestions(emf);
        usersAndRoles(emf);
    }

    private static void usersAndRoles(EntityManagerFactory emf){
        UserDao userDao = UserDao.getInstance();
        RoleDao roleDao = RoleDao.getInstance();

        User user1 = new User("Villager", "1234");
        User user2 = new User("PineBoxJim","4321");

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

    }
}
